import 'dart:io';

import 'package:android_intent_plus/android_intent.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:Safe_pulse/Completeprofile.dart';
import 'package:Safe_pulse/Pages/LinkUserPage.dart';
import 'package:Safe_pulse/Pages/LocationHistoryPage.dart';
import 'package:Safe_pulse/Pages/MultilePeoplesHistory.dart';
import 'package:Safe_pulse/Pages/ProfilePage%20.dart';
import 'package:Safe_pulse/Pages/locationhistory24.dart';
import 'package:Safe_pulse/other%20pages/LiveTrackingPage.dart';
import 'package:Safe_pulse/services/permission_helper.dart';
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:cached_network_image/cached_network_image.dart';

class UserRelationsPage extends StatefulWidget {
  const UserRelationsPage({Key? key}) : super(key: key);

  @override
  _UserRelationsPageState createState() => _UserRelationsPageState();
}

class _UserRelationsPageState extends State<UserRelationsPage> {
  List<dynamic> _relations = [];
  bool _isLoading = false;
  String _errorMessage = "";
  String? _requestingUserId;
  String? _userEmail;
  String? _userName;
  List<String> _emailsToTrack = [];
  Map<String, bool> _selectedUsers = {};
  String _selectedDuration = '2 hours';
  List<String> userIdsToTrack2 = [];
  final List<String> _durationOptions = [
    '2 hours',
    '6 hours',
    '12 hours',
    '24 hours'
  ];

  // Sample profile image URLs
  final Map<String, String> _relationImages = {
    'child':
        'https://images.unsplash.com/photo-1519683109079-d5f539e1542f?w=200&auto=format&fit=crop',
    'parent':
        'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=200&auto=format&fit=crop',
    'spouse':
        'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200&auto=format&fit=crop',
    'friend':
        'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop',
    'default':
        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop',
  };
  Future<void> _requestPermissions() async {
    await PermissionHelper.requestPermissions(context);
  }

  final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  String _fcmToken = "";
  @override
  void initState() {
    super.initState();
    _loadUserData();
    _loadProfile();

    _requestPermissions();
    disableBatteryOptimization();
    _initializeApp();
    _setupPushNotifications();
  }

  String userId = "";
  Future<void> _initializeApp() async {
    try {
      // Request notification permissions
      await _firebaseMessaging.requestPermission(
        alert: true,
        announcement: false,
        badge: true,
        carPlay: false,
        criticalAlert: false,
        provisional: false,
        sound: true,
      );

      // Get FCM token and save it
      final token = await FirebaseMessaging.instance.getToken();
      setState(() {
        _fcmToken = token!;
      });
      _apiService.updateNotificationToken(token!);
      debugPrint('Initial FCM Token: $token');

      // Check auth status
      final prefs = await SharedPreferences.getInstance();
      final accessToken = prefs.getString('access_token');

      final username = prefs.getString('username') ?? '';
      userId = prefs.getInt('user_id')?.toString() ?? '0';
      final role = prefs.getString('role') ?? '';
    } catch (e) {
      debugPrint('Initialization error: $e');
    }
  }

  Future<void> _showCustomDialog(
    BuildContext context, {
    required String title,
    required String message,
    bool isSuccess = false,
    Function()? onConfirm,
    bool showCancel = false,
    Function()? onCancel,
  }) async {
    await showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        title: Text(
          title,
          style: TextStyle(
            color: Colors.black87,
            fontWeight: FontWeight.bold,
          ),
        ),
        content: Text(
          message,
          style: TextStyle(color: Colors.black87.withOpacity(0.8)),
        ),
        actions: [
          if (showCancel)
            TextButton(
              onPressed: () {
                Navigator.pop(context);
                if (onCancel != null) onCancel();
              },
              child: Text(
                'Cancel',
                style: TextStyle(
                  color: Colors.red,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              if (onConfirm != null) onConfirm();
            },
            child: Text(
              'OK',
              style: TextStyle(
                color: Colors.blue,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _setupPushNotifications() {
    // Handle token refresh
    _firebaseMessaging.onTokenRefresh.listen((newToken) async {
      debugPrint('FCM Token refreshed: $newToken');
      // _apiService.updateNotificationToken(newToken);
      final prefs = await SharedPreferences.getInstance();
      final accessToken = prefs.getString('access_token');
      if (accessToken != null) {
        // await _sendTokenToServer(newToken, accessToken);
      }
    });

    // Handle foreground messages
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      if (message.notification != null && mounted) {
        _showCustomDialog(
          context,
          title: message.notification?.title ?? 'Notification',
          message: message.notification?.body ?? '',
          isSuccess: true,
        );
      }
      print(
          '✅ Notification opened from background/terminated state and displayed');
    });

    // Handle when app is opened from terminated state
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      if (message.notification != null && mounted) {
        _showCustomDialog(
          context,
          title: message.notification?.title ?? 'Notification',
          message: message.notification?.body ?? '',
          isSuccess: true,
        );
      }
      print(
          '✅ Notification opened from background/terminated state and displayed2');
    });

    // Get initial message if app was launched from notification
    FirebaseMessaging.instance
        .getInitialMessage()
        .then((RemoteMessage? message) {
      if (message != null && mounted) {
        _showCustomDialog(
          context,
          title: message.notification?.title ?? 'Notification',
          message: message.notification?.body ?? '',
          isSuccess: true,
        );
      }
    });
  }

  void disableBatteryOptimization() {
    if (Platform.isAndroid) {
      AndroidIntent intent = AndroidIntent(
        action: 'android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS',
        data: 'package:com.otoi.safe_pulse', // Replace with your package name
      );
      intent.launch();
    }
  }

  Future<void> _loadProfile() async {
    try {
      final profile = await _apiService.getUserProfile();
      setState(() {
        _userProfile = profile;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });

      // Navigate to CreateProfilePage if an error occurs
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => EditProfilePage(
            userProfile: null,
            onProfileUpdated: _loadProfile,
          ),
        ),
      );
    }
  }

  final ApiService _apiService = ApiService();
  Map<String, dynamic>? _userProfile;
  Future<void> _loadUserData() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _requestingUserId = prefs.getString("userId");
      _userEmail = prefs.getString("username");
      _userName = prefs.getString("name") ?? "User";
    });

    if (_userEmail != null) {
      _fetchRelations();
    } else {
      setState(() {
        _errorMessage = "User email not found";
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchRelations() async {
    if (_userEmail == null) return;

    setState(() {
      _isLoading = true;
      _errorMessage = "";
    });

    try {
      final response = await http.get(Uri.parse(
          '${ApiService.baseUrl}/api/relationships/getUserRelations?email=$_userEmail'));

      if (response.statusCode == 200) {
        print("dfcsvdcghbnbfgh n ${response.body}");
        final data = json.decode(response.body);
        setState(() {
          _relations = data;
          _emailsToTrack = data
              .where((relation) =>
                  (relation['activityStatus'] == 'Not Active' ||
                      relation['activityStatus'] == null) &&
                  relation['relationDirection'] == 'connected')
              .map<String>((relation) => relation['otherUserEmail'].toString())
              .toList();
          userIdsToTrack2 = data
              .map<String>((relation) => relation['userRelationId'].toString())
              .toList();
          // Initialize selected users map with emails as keys
          for (var relation in data) {
            _selectedUsers[relation['otherUserEmail'].toString()] = false;
          }
        });
      } else {
        setState(() {
          _errorMessage =
              "Failed to fetch relations. Status code: ${response.statusCode}";
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = "Error fetching relations: $e";
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _navigateToTrackingPage(List<String> emailsToTrack, String duration) {
    if (_userEmail == null || emailsToTrack.isEmpty) return;
    print("sdgdfghfg $_userEmail");
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => MultilePeoplesHistory(
          requestingUserId: _userEmail!,
          emailsToTrack: emailsToTrack,
          duration: duration,
        ),
      ),
    );
  }

  void _navigateToTrackingPageAll(List<String> emailsToTrack) {
    if (_userEmail == null || emailsToTrack.isEmpty) return;

    // Get the user IDs for the emails to track
    final userIdsToTrack = _relations
        .where((relation) => emailsToTrack.contains(relation['otherUserEmail']))
        .map<String>((relation) => relation['userRelationId'].toString())
        .toList();

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => LiveTrackingPagemain(
          requestingUserId: userId,
          userIdsToTrack: userIdsToTrack,
        ),
      ),
    );
  }

  void _moveToHistory(String mail) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => LocationHistoryPage(
          mail: mail,
        ),
      ),
    );
  }

  void _moveTo24HourHistory(String mail) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => LocationHistoryPageWF(
          mail: mail,
        ),
      ),
    );
  }

  String _getRelationImage(String relationName) {
    final key = relationName.toLowerCase();
    return _relationImages.containsKey(key)
        ? _relationImages[key]!
        : _relationImages['default']!;
  }

  Widget _buildTrackAllRow() {
    final selectedCount =
        _selectedUsers.values.where((isSelected) => isSelected).length;
    final allSelected =
        selectedCount == _selectedUsers.length && _selectedUsers.isNotEmpty;

    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(15),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 2,
            blurRadius: 5,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Padding(
        padding: EdgeInsets.all(12),
        child: Row(
          children: [
            // Track All Button
            Expanded(
              child: ElevatedButton.icon(
                onPressed: () {
                  setState(() {
                    final newValue = !allSelected;
                    _selectedUsers.updateAll((key, value) => newValue);
                  });
                },
                icon: Icon(
                    allSelected ? Icons.check_circle : Icons.track_changes),
                label: Text(allSelected ? 'Deselect All' : 'Select All'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color(0xFF5C6BC0),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  padding: EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
            SizedBox(width: 10),
            // Duration Dropdown
            Container(
              padding: EdgeInsets.symmetric(horizontal: 12),
              decoration: BoxDecoration(
                color: Colors.grey[100],
                borderRadius: BorderRadius.circular(12),
              ),
              child: DropdownButton<String>(
                value: _selectedDuration,
                underline: Container(),
                items: _durationOptions.map((String value) {
                  return DropdownMenuItem<String>(
                    value: value,
                    child: Text(value),
                  );
                }).toList(),
                onChanged: (newValue) {
                  setState(() {
                    _selectedDuration = newValue!;
                  });
                },
              ),
            ),
            SizedBox(width: 10),
            // Track Selected Button
            ElevatedButton(
              onPressed: selectedCount > 0
                  ? () {
                      final selectedEmails = _selectedUsers.entries
                          .where((entry) => entry.value)
                          .map((entry) => entry.key)
                          .toList();
                      _navigateToTrackingPage(
                          selectedEmails, _selectedDuration);
                    }
                  : null,
              child: Text('Track'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                padding: EdgeInsets.symmetric(vertical: 12, horizontal: 16),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSOSButtons() {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          ElevatedButton.icon(
            onPressed: () => _navigateToTrackingPageAll(_emailsToTrack),
            icon: Icon(Icons.track_changes, color: Colors.white),
            label: Text(
              'Track All',
              style: TextStyle(color: Colors.white),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.blue,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
              padding: EdgeInsets.symmetric(horizontal: 18, vertical: 11),
            ),
          ),
          ElevatedButton.icon(
            onPressed: () {
              // Handle SOS alert
            },
            icon: Icon(Icons.emergency, color: Colors.white),
            label: Text(
              'Emergency',
              style: TextStyle(color: Colors.white),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
              padding: EdgeInsets.symmetric(horizontal: 18, vertical: 11),
            ),
          ),
          ElevatedButton.icon(
            onPressed: () {
              // Handle SOS alert
            },
            icon: Icon(Icons.warning, color: Colors.white),
            label: Text(
              'SOS Alert',
              style: TextStyle(color: Colors.white),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.orange,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
              padding: EdgeInsets.symmetric(horizontal: 18, vertical: 11),
            ),
          ),
        ],
      ),
    );
  }

  // Widget _buildRelationCard(
  //     Map<String, dynamic> relation, Color color, bool isTrackable) {
  //   final relationName = relation['relationName'] ?? 'Connection';
  //   final email = relation['otherUserEmail'] ?? 'Unknown email';
  //   final isConnected = relation['isLinked'] == 1.0;
  //   final status = relation['activityStatus'] ?? 'Unknown';

  //   return Container(
  //     margin: EdgeInsets.symmetric(vertical: 4, horizontal: 16),
  //     decoration: BoxDecoration(
  //       color: Colors.white,
  //       borderRadius: BorderRadius.circular(15),
  //       boxShadow: [
  //         BoxShadow(
  //           color: Colors.black12,
  //           blurRadius: 4,
  //           offset: Offset(0, 2),
  //         ),
  //       ],
  //     ),
  //     child: InkWell(
  //       borderRadius: BorderRadius.circular(15),
  //       onTap: isTrackable
  //           ? () => _navigateToTrackingPage([email], _selectedDuration)
  //           : null,
  //       child: Row(
  //         children: [
  //           // Checkbox
  //           Checkbox(
  //             value: _selectedUsers[email] ?? false,
  //             onChanged: isTrackable
  //                 ? (value) {
  //                     setState(() {
  //                       _selectedUsers[email] = value!;
  //                     });
  //                   }
  //                 : null,
  //             activeColor: Color(0xFF5C6BC0),
  //           ),
  //           SizedBox(width: 8),
  //           // Profile image with status indicator
  //           Stack(
  //             children: [
  //               Container(
  //                 width: 50,
  //                 // height: 50,
  //                 decoration: BoxDecoration(
  //                   borderRadius: BorderRadius.circular(12),
  //                 ),
  //                 child: ClipRRect(
  //                   borderRadius: BorderRadius.circular(12),
  //                   child: CachedNetworkImage(
  //                     imageUrl: _getRelationImage(relationName),
  //                     fit: BoxFit.cover,
  //                     placeholder: (context, url) => Container(
  //                       color: Colors.grey[200],
  //                       child: Icon(Icons.person, color: Colors.grey[400]),
  //                     ),
  //                     errorWidget: (context, url, error) => Icon(Icons.person),
  //                   ),
  //                 ),
  //               ),
  //               if (isTrackable)
  //                 Positioned(
  //                   bottom: 0,
  //                   right: 0,
  //                   child: Container(
  //                     padding: EdgeInsets.all(2),
  //                     decoration: BoxDecoration(
  //                       color: Colors.green,
  //                       shape: BoxShape.circle,
  //                       border: Border.all(color: Colors.white, width: 2),
  //                     ),
  //                     child: Icon(Icons.location_on,
  //                         size: 12, color: Colors.white),
  //                   ),
  //                 ),
  //             ],
  //           ),
  //           SizedBox(width: 16),
  //           Expanded(
  //             child: Column(
  //               crossAxisAlignment: CrossAxisAlignment.start,
  //               children: [
  //                 Text(
  //                   relationName,
  //                   style: TextStyle(
  //                     fontSize: 16,
  //                     fontWeight: FontWeight.bold,
  //                     color: Colors.grey[800],
  //                   ),
  //                 ),
  //                 // SizedBox(height: 4),
  //                 // Text(
  //                 //   email,
  //                 //   style: TextStyle(
  //                 //     fontSize: 14,
  //                 //     color: Colors.grey[600],
  //                 //   ),
  //                 //   overflow: TextOverflow.ellipsis,
  //                 // ),
  //                 // SizedBox(height: 4),
  //                 Row(
  //                   children: [
  //                     Container(
  //                       padding:
  //                           EdgeInsets.symmetric(horizontal: 8, vertical: 2),
  //                       decoration: BoxDecoration(
  //                         color: isConnected
  //                             ? Colors.green[50]
  //                             : Colors.orange[50],
  //                         borderRadius: BorderRadius.circular(10),
  //                       ),
  //                       child: Row(
  //                         mainAxisSize: MainAxisSize.min,
  //                         children: [
  //                           Icon(
  //                             isConnected ? Icons.check_circle : Icons.pending,
  //                             size: 14,
  //                             color: isConnected ? Colors.green : Colors.orange,
  //                           ),
  //                           SizedBox(width: 4),
  //                           Text(
  //                             isConnected ? 'Connected' : 'Pending',
  //                             style: TextStyle(
  //                               fontSize: 12,
  //                               color:
  //                                   isConnected ? Colors.green : Colors.orange,
  //                             ),
  //                           ),
  //                         ],
  //                       ),
  //                     ),
  //                     SizedBox(width: 8),
  //                     if (status != 'Unknown')
  //                       Container(
  //                         padding:
  //                             EdgeInsets.symmetric(horizontal: 8, vertical: 2),
  //                         decoration: BoxDecoration(
  //                           color: status == 'Active'
  //                               ? Colors.blue[50]
  //                               : Colors.grey[100],
  //                           borderRadius: BorderRadius.circular(10),
  //                         ),
  //                         child: Text(
  //                           status,
  //                           style: TextStyle(
  //                             fontSize: 12,
  //                             color: status == 'Active'
  //                                 ? Colors.blue
  //                                 : Colors.grey,
  //                           ),
  //                         ),
  //                       ),
  //                   ],
  //                 ),
  //               ],
  //             ),
  //           ),
  //           // Action buttons
  //           Column(
  //             mainAxisSize: MainAxisSize.min,
  //             children: [
  //               if (isTrackable)
  //                 IconButton(
  //                   icon: Icon(Icons.location_searching, color: Colors.blue),
  //                   onPressed: () => _navigateToTrackingPageAll([email]),
  //                   tooltip: 'Live Tracking',
  //                 ),
  //               IconButton(
  //                 icon: Icon(Icons.history, color: Colors.purple),
  //                 onPressed: () => _moveTo24HourHistory(email),
  //                 tooltip: 'Location History',
  //               ),
  //             ],
  //           ),
  //         ],
  //       ),
  //     ),
  //   );
  // }



Widget _buildRelationCard(
    Map<String, dynamic> relation, Color color, bool isTrackable) {
  final relationName = relation['relationName'] ?? 'Connection';
  final email = relation['otherUserEmail'] ?? 'Unknown email';
  final isConnected = relation['isLinked'] == 1.0;
  final status = relation['activityStatus'] ?? 'Unknown';
  final imageUrl = relation['imageUrl'] ?? '';

  // Construct full URL if the imageUrl is relative
  final fullImageUrl = imageUrl.isNotEmpty
      ? imageUrl.startsWith('http')
          ? imageUrl
          : '${ApiService.baseUrl}/$imageUrl'
      : _getRelationImage(relationName);

  return Container(
    margin: EdgeInsets.symmetric(vertical: 4, horizontal: 16),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(15),
      boxShadow: [
        BoxShadow(
          color: Colors.black12,
          blurRadius: 4,
          offset: Offset(0, 2),
        ),
      ],
    ),
    child: InkWell(
      borderRadius: BorderRadius.circular(15),
      onTap: isTrackable
          ? () => _navigateToTrackingPage([email], _selectedDuration)
          : null,
      child: Row(
        children: [
          // Checkbox
          Checkbox(
            value: _selectedUsers[email] ?? false,
            onChanged: isTrackable
                ? (value) {
                    setState(() {
                      _selectedUsers[email] = value!;
                    });
                  }
                : null,
            activeColor: Color(0xFF5C6BC0),
          ),
          SizedBox(width: 8),
          // Profile image with status indicator
          Stack(
            children: [
              Container(
                width: 50,
                height: 50,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(12),
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: CachedNetworkImage(
                    imageUrl: fullImageUrl,
                    fit: BoxFit.cover,
                    placeholder: (context, url) => Container(
                      color: Colors.grey[200],
                      child: Icon(Icons.person, color: Colors.grey[400]),
                    ),
                    errorWidget: (context, url, error) => 
                        Image.asset('assets/images/default_user.png'),
                  ),
                ),
              ),
              if (isTrackable)
                Positioned(
                  bottom: 0,
                  right: 0,
                  child: Container(
                    padding: EdgeInsets.all(2),
                    decoration: BoxDecoration(
                      color: Colors.green,
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.white, width: 2),
                    ),
                    child: Icon(Icons.location_on,
                        size: 12, color: Colors.white),
                  ),
                ),
            ],
          ),
          SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  relationName,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Colors.grey[800],
                  ),
                ),
                Row(
                  children: [
                    Container(
                      padding:
                          EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: isConnected
                            ? Colors.green[50]
                            : Colors.orange[50],
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            isConnected ? Icons.check_circle : Icons.pending,
                            size: 14,
                            color: isConnected ? Colors.green : Colors.orange,
                          ),
                          SizedBox(width: 4),
                          Text(
                            isConnected ? 'Connected' : 'Pending',
                            style: TextStyle(
                              fontSize: 12,
                              color:
                                  isConnected ? Colors.green : Colors.orange,
                            ),
                          ),
                        ],
                      ),
                    ),
                    SizedBox(width: 8),
                    if (status != 'Unknown')
                      Container(
                        padding:
                            EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: status == 'Active'
                              ? Colors.blue[50]
                              : Colors.grey[100],
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          status,
                          style: TextStyle(
                            fontSize: 12,
                            color: status == 'Active'
                                ? Colors.blue
                                : Colors.grey,
                          ),
                        ),
                      ),
                  ],
                ),
              ],
            ),
          ),
          // Action buttons
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (isTrackable)
                IconButton(
                  icon: Icon(Icons.location_searching, color: Colors.blue),
                  onPressed: () => _navigateToTrackingPageAll([email]),
                  tooltip: 'Live Tracking',
                ),
              IconButton(
                icon: Icon(Icons.history, color: Colors.purple),
                onPressed: () => _moveTo24HourHistory(email),
                tooltip: 'Location History',
              ),
            ],
          ),
        ],
      ),
    ),
  );
}


  // Widget _buildRelationCard(
  //     Map<String, dynamic> relation, Color color, bool isTrackable) {
  //   final relationName = relation['relationName'] ?? 'Connection';
  //   final email = relation['otherUserEmail'] ?? 'Unknown email';
  //   final isConnected = relation['isLinked'] == 1.0;
  //   final status = relation['activityStatus'] ?? 'Unknown';

  //   return Card(
  //     margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
  //     elevation: 2,
  //     shape: RoundedRectangleBorder(
  //       borderRadius: BorderRadius.circular(15),
  //     ),
  //     child: InkWell(
  //       borderRadius: BorderRadius.circular(15),
  //       onTap: isTrackable
  //           ? () => _navigateToTrackingPage([email], _selectedDuration)
  //           : null,
  //       child: Padding(
  //         padding: EdgeInsets.all(12),
  //         child: Row(
  //           children: [
  //             // Checkbox
  //             Checkbox(
  //               value: _selectedUsers[email] ?? false,
  //               onChanged: isTrackable
  //                   ? (value) {
  //                       setState(() {
  //                         _selectedUsers[email] = value!;
  //                       });
  //                     }
  //                   : null,
  //               activeColor: Color(0xFF5C6BC0),
  //             ),
  //             SizedBox(width: 8),
  //             // Profile image with status indicator
  //             Stack(
  //               children: [
  //                 Container(
  //                   width: 50,
  //                   height: 50,
  //                   decoration: BoxDecoration(
  //                     borderRadius: BorderRadius.circular(12),
  //                   ),
  //                   child: ClipRRect(
  //                     borderRadius: BorderRadius.circular(12),
  //                     child: CachedNetworkImage(
  //                       imageUrl: _getRelationImage(relationName),
  //                       fit: BoxFit.cover,
  //                       placeholder: (context, url) => Container(
  //                         color: Colors.grey[200],
  //                         child: Icon(Icons.person, color: Colors.grey[400]),
  //                       ),
  //                       errorWidget: (context, url, error) =>
  //                           Icon(Icons.person),
  //                     ),
  //                   ),
  //                 ),
  //                 if (isTrackable)
  //                   Positioned(
  //                     bottom: 0,
  //                     right: 0,
  //                     child: Container(
  //                       padding: EdgeInsets.all(4),
  //                       decoration: BoxDecoration(
  //                         color: Colors.green,
  //                         shape: BoxShape.circle,
  //                         border: Border.all(color: Colors.white, width: 2),
  //                       ),
  //                       child: Icon(Icons.location_on,
  //                           size: 12, color: Colors.white),
  //                     ),
  //                   ),
  //               ],
  //             ),
  //             SizedBox(width: 16),
  //             Expanded(
  //               child: Column(
  //                 crossAxisAlignment: CrossAxisAlignment.start,
  //                 children: [
  //                   Text(
  //                     relationName,
  //                     style: TextStyle(
  //                       fontSize: 16,
  //                       fontWeight: FontWeight.bold,
  //                       color: Colors.grey[800],
  //                     ),
  //                   ),
  //                   SizedBox(height: 4),
  //                   Text(
  //                     email,
  //                     style: TextStyle(
  //                       fontSize: 14,
  //                       color: Colors.grey[600],
  //                     ),
  //                     overflow: TextOverflow.ellipsis,
  //                   ),
  //                   SizedBox(height: 4),
  //                   Row(
  //                     children: [
  //                       Container(
  //                         padding:
  //                             EdgeInsets.symmetric(horizontal: 8, vertical: 2),
  //                         decoration: BoxDecoration(
  //                           color: isConnected
  //                               ? Colors.green[50]
  //                               : Colors.orange[50],
  //                           borderRadius: BorderRadius.circular(10),
  //                         ),
  //                         child: Row(
  //                           mainAxisSize: MainAxisSize.min,
  //                           children: [
  //                             Icon(
  //                               isConnected
  //                                   ? Icons.check_circle
  //                                   : Icons.pending,
  //                               size: 14,
  //                               color:
  //                                   isConnected ? Colors.green : Colors.orange,
  //                             ),
  //                             SizedBox(width: 4),
  //                             Text(
  //                               isConnected ? 'Connected' : 'Pending',
  //                               style: TextStyle(
  //                                 fontSize: 12,
  //                                 color: isConnected
  //                                     ? Colors.green
  //                                     : Colors.orange,
  //                               ),
  //                             ),
  //                           ],
  //                         ),
  //                       ),
  //                       SizedBox(width: 8),
  //                       if (status != 'Unknown')
  //                         Container(
  //                           padding: EdgeInsets.symmetric(
  //                               horizontal: 8, vertical: 2),
  //                           decoration: BoxDecoration(
  //                             color: status == 'Active'
  //                                 ? Colors.blue[50]
  //                                 : Colors.grey[100],
  //                             borderRadius: BorderRadius.circular(10),
  //                           ),
  //                           child: Text(
  //                             status,
  //                             style: TextStyle(
  //                               fontSize: 12,
  //                               color: status == 'Active'
  //                                   ? Colors.blue
  //                                   : Colors.grey,
  //                             ),
  //                           ),
  //                         ),
  //                     ],
  //                   ),
  //                 ],
  //               ),
  //             ),
  //             // Action buttons
  //             Column(
  //               mainAxisSize: MainAxisSize.min,
  //               children: [
  //                 if (isTrackable)
  //                   IconButton(
  //                     icon: Icon(Icons.location_searching, color: Colors.blue),
  //                     onPressed: () =>
  //                         _navigateToTrackingPage([email], _selectedDuration),
  //                     tooltip: 'Live Tracking',
  //                   ),
  //                 IconButton(
  //                   icon: Icon(Icons.history, color: Colors.purple),
  //                   onPressed: () => _moveTo24HourHistory(email),
  //                   tooltip: 'Location History',
  //                 ),
  //               ],
  //             ),
  //           ],
  //         ),
  //       ),
  //     ),
  //   );
  // }

  @override
  Widget build(BuildContext context) {
    final trackableRelations = _relations
        .where((relation) =>
            (relation['activityStatus'] == 'Not Active' ||
                relation['activityStatus'] == null) &&
            relation['relationDirection'] == 'connected')
        .toList();

    final otherRelations = _relations
        .where((relation) => !((relation['activityStatus'] == 'Not Active' ||
                relation['activityStatus'] == null) &&
            relation['relationDirection'] == 'connected'))
        .toList();

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text(
          'Dashboard',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        centerTitle: true,
        backgroundColor: Color(0xFF5C6BC0),
        elevation: 0,
      ),
      body: _isLoading
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(
                    valueColor:
                        AlwaysStoppedAnimation<Color>(Color(0xFF5C6BC0)),
                  ),
                  SizedBox(height: 16),
                  Text(
                    'Loading connections...',
                    style: TextStyle(
                      color: Colors.grey[600],
                      fontSize: 16,
                    ),
                  ),
                ],
              ),
            )
          : _errorMessage.isNotEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.error_outline,
                        color: Colors.red,
                        size: 48,
                      ),
                      SizedBox(height: 16),
                      Text(
                        _errorMessage,
                        style: TextStyle(
                          color: Colors.red,
                          fontSize: 16,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _fetchRelations,
                        child: Text('Retry'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Color(0xFF5C6BC0),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                      ),
                    ],
                  ),
                )
              : _userEmail == null
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.warning_amber,
                            color: Colors.orange,
                            size: 48,
                          ),
                          SizedBox(height: 16),
                          Text(
                            "User information not available",
                            style: TextStyle(
                              color: Colors.grey[700],
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _fetchRelations,
                      color: Color(0xFF5C6BC0),
                      child: CustomScrollView(
                        slivers: [
                          SliverToBoxAdapter(
                            child: Container(
                              padding: EdgeInsets.all(20),
                              decoration: BoxDecoration(
                                color: Color(0xFF5C6BC0),
                                borderRadius: BorderRadius.only(
                                  bottomLeft: Radius.circular(30),
                                  bottomRight: Radius.circular(30),
                                ),
                              ),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    "Hello, ${_userProfile != null ? "${_userProfile!['firstName']} ${_userProfile!['lastName']}" : _userName ?? "User"}!",
                                    style: TextStyle(
                                      fontSize: 24,
                                      fontWeight: FontWeight.bold,
                                      color: Colors.white,
                                    ),
                                  ),
                                  SizedBox(height: 10),
                                  Text(
                                    "Keep your loved ones safe with real-time tracking",
                                    style: TextStyle(
                                      fontSize: 16,
                                      color: Colors.white.withOpacity(0.9),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                          SliverToBoxAdapter(
                            child: _buildSOSButtons(),
                          ),
                          if (trackableRelations.isNotEmpty) ...[
                            SliverToBoxAdapter(
                              child: _buildTrackAllRow(),
                            ),
                            SliverPadding(
                              padding: EdgeInsets.fromLTRB(24, 8, 24, 8),
                              sliver: SliverToBoxAdapter(
                                child: Text(
                                  'ACTIVE CONNECTIONS',
                                  style: TextStyle(
                                    fontSize: 14,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.grey[600],
                                    letterSpacing: 1,
                                  ),
                                ),
                              ),
                            ),
                            SliverList(
                              delegate: SliverChildBuilderDelegate(
                                (context, index) => _buildRelationCard(
                                    trackableRelations[index],
                                    Color(0xFF5C6BC0),
                                    true),
                                childCount: trackableRelations.length,
                              ),
                            ),
                          ],
                          if (otherRelations.isNotEmpty) ...[
                            SliverPadding(
                              padding: EdgeInsets.fromLTRB(24, 16, 24, 8),
                              sliver: SliverToBoxAdapter(
                                child: Text(
                                  'OTHER CONNECTIONS',
                                  style: TextStyle(
                                    fontSize: 14,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.grey[600],
                                    letterSpacing: 1,
                                  ),
                                ),
                              ),
                            ),
                            SliverList(
                              delegate: SliverChildBuilderDelegate(
                                (context, index) => _buildRelationCard(
                                    otherRelations[index],
                                    Colors.grey[600]!,
                                    false),
                                childCount: otherRelations.length,
                              ),
                            ),
                          ],
                          if (_relations.isEmpty)
                            SliverFillRemaining(
                              child: Center(
                                child: Padding(
                                  padding: EdgeInsets.all(32),
                                  child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Image.asset(
                                        'assets/images/no_connections.png',
                                        width: 150,
                                        height: 150,
                                        color: Colors.grey[400],
                                      ),
                                      SizedBox(height: 16),
                                      Text(
                                        'No connections yet',
                                        style: TextStyle(
                                          fontSize: 18,
                                          color: Colors.grey[600],
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                      SizedBox(height: 8),
                                      Text(
                                        'Add family members or friends to start tracking',
                                        style: TextStyle(
                                          fontSize: 14,
                                          color: Colors.grey[500],
                                        ),
                                        textAlign: TextAlign.center,
                                      ),
                                      SizedBox(height: 16),
                                      ElevatedButton(
                                        onPressed: () async {
                                          SharedPreferences prefs =
                                              await SharedPreferences
                                                  .getInstance();
                                          String? email =
                                              prefs.getString("username");
                                          if (email != null) {
                                            Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                builder: (context) =>
                                                    LinkUserPage(
                                                        userEmail: email),
                                              ),
                                            );
                                          }
                                        },
                                        child: Text('Add Connection'),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Color(0xFF5C6BC0),
                                          shape: RoundedRectangleBorder(
                                            borderRadius:
                                                BorderRadius.circular(12),
                                          ),
                                          padding: EdgeInsets.symmetric(
                                              horizontal: 24, vertical: 12),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                          SliverToBoxAdapter(
                            child: _buildEmergencySection(),
                          ),
                        ],
                      ),
                    ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          SharedPreferences prefs = await SharedPreferences.getInstance();
          String? email = prefs.getString("username");

          if (email != null) {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => LinkUserPage(userEmail: email),
              ),
            );
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text("No email found!")),
            );
          }
        },
        child: Icon(Icons.person_add_alt_1, color: Colors.white),
        backgroundColor: Color(0xFF5C6BC0),
        elevation: 4,
      ),
    );
  }

  // Add this widget method to your _UserRelationsPageState class
  Widget _buildEmergencySection() {
    return Container(
      margin: EdgeInsets.all(16),
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.red[50],
        borderRadius: BorderRadius.circular(15),
        border: Border.all(color: Colors.red[100]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Emergency Alert',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.red[800],
            ),
          ),
          SizedBox(height: 8),
          Text(
            'In case of emergency, send an alert to all your connections',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[700],
            ),
          ),
          SizedBox(height: 12),
          ElevatedButton.icon(
            onPressed: () {
              _showEmergencyConfirmationDialog();
            },
            icon: Icon(Icons.warning, size: 24),
            label: Text('SEND EMERGENCY ALERT'),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
              padding: EdgeInsets.symmetric(vertical: 14),
              minimumSize: Size(double.infinity, 0),
            ),
          ),
        ],
      ),
    );
  }

// Add this method to show confirmation dialog
  void _showEmergencyConfirmationDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Emergency Alert'),
          content: Text(
            'Are you sure you want to send an emergency alert to all your connections?',
            style: TextStyle(fontSize: 16),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                _sendEmergencyAlert();
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
              ),
              child: Text('Send Alert'),
            ),
          ],
        );
      },
    );
  }

// Add this method to handle the emergency alert
  Future<void> _sendEmergencyAlert() async {
    if (_userEmail == null) return;

    setState(() {
      _isLoading = true;
    });

    try {
      // Replace with your actual API endpoint
      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/api/emergency/sendAlert'),
        body: json.encode({
          'userEmail': _userEmail,
          'timestamp': DateTime.now().toIso8601String(),
        }),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Emergency alert sent successfully!'),
            backgroundColor: Colors.green,
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to send emergency alert. Please try again.'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error sending alert: $e'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }
}
