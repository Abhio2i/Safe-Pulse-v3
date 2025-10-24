import 'dart:math';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'dart:convert';
import 'package:intl/intl.dart';
import 'package:uuid/uuid.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:vibration/vibration.dart';
import 'package:flutter_tts/flutter_tts.dart';



class LiveTrackingPagemain extends StatefulWidget {
  final String requestingUserId;
  final List<String> userIdsToTrack;

  const LiveTrackingPagemain({
    Key? key,
    required this.requestingUserId,
    required this.userIdsToTrack,
  }) : super(key: key);

  @override
  _LiveTrackingPagemainState createState() => _LiveTrackingPagemainState();
}

class _LiveTrackingPagemainState extends State<LiveTrackingPagemain> {
  late GoogleMapController _mapController;
  final Set<Marker> _markers = {};
  final Set<Circle> _footprints = {};
  final Map<String, Set<Polyline>> _userPathLines = {};
  final Map<String, List<LatLng>> _userPathPoints = {};
  final Map<String, double> _userDistances = {};
  final Map<String, DateTime?> _userLastUpdateTimes = {};
  final Map<String, Color> _userColors = {};
  final Map<String, LatLng?> _userLastPositions = {};
    // Add this audio player instance
  final AudioPlayer _audioPlayer = AudioPlayer();
  bool _isPlayingAlertSound = false;
    // Add this TTS instance
  final FlutterTts _tts = FlutterTts();
  bool _isSpeaking = false;


  late StompClient _stompClient;
  bool _isConnected = false;
  DateTime? _trackingStartTime;
  final ScrollController _scrollController = ScrollController();

  // Alert related variables
  final List<Map<String, dynamic>> _alerts = [];
  bool _showAlertPanel = false;
  final _alertController = ScrollController();

  // Color palette for different users
  final List<Color> _colorPalette = [
    Colors.blue,
    Colors.red,
    Colors.green,
    Colors.purple,
    Colors.orange,
    Colors.teal,
  ];

  // Add these constants at the top of your file
static const _slowSpeechRate = 0.4;
static const _normalSpeechRate = 0.5;
static const _volume = 1.0;

  @override
  void initState() {
    super.initState();
    _initializeTts();
    _trackingStartTime = DateTime.now();

    // Initialize user data structures
    for (var userId in widget.userIdsToTrack) {
      _userPathPoints[userId] = [];
      _userPathLines[userId] = {};
      _userDistances[userId] = 0.0;
      _userLastUpdateTimes[userId] = null;
      _userLastPositions[userId] = null;
      _userColors[userId] = _colorPalette[
          widget.userIdsToTrack.indexOf(userId) % _colorPalette.length];
    }

    _connectWebSocket();
  }

  

    Future<void> _initializeTts() async {
    await _tts.setLanguage("en-US");
    await _tts.setSpeechRate(0.5);
    await _tts.setVolume(1.0);
  }

  @override
  void dispose() {
    _stompClient.deactivate();
    _scrollController.dispose();
    _alertController.dispose();
    _audioPlayer.dispose(); // Dispose the audio player when widget is disposed
    _tts.stop();
    super.dispose();
  }

  void _connectWebSocket() {
    _stompClient = StompClient(
      config: StompConfig.sockJS(
        url: '${ApiService.baseUrl}/ws-location',
        onConnect: (frame) {
          setState(() => _isConnected = true);
          _sendTrackingRequests();
          _subscribeToLocationUpdatesHistory();
          _subscribeToLocationUpdates();
          _subscribeToAlerts(); // Add alert subscription
        },
        onDisconnect: (frame) => setState(() => _isConnected = false),
        reconnectDelay: Duration(seconds: 5),
      ),
    );
    _stompClient.activate();
  }

  void _subscribeToLocationUpdates() {
    _stompClient.subscribe(
      destination: '/topic/location-updates',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final data = jsonDecode(frame.body!);
            final userId = data['user']['userId'];

            if (widget.userIdsToTrack.contains(userId)) {
              final lat = data['latitude']?.toDouble() ?? 0.0;
              final lng = data['longitude']?.toDouble() ?? 0.0;
              final position = LatLng(lat, lng);

              setState(() {
                _userLastUpdateTimes[userId] = DateTime.now();
                _userLastPositions[userId] = position;
                _updatePath(userId, position);
              });

              _updateMap(userId, position);
              _addFootprint(userId, position);
            }
          } catch (e) {
            print('Error processing location: $e');
          }
        }
      },
    );
  }

  void _subscribeToLocationUpdatesHistory() {
    _stompClient.subscribe(
      destination: '/topic/user-locations',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final Map<String, dynamic> data = jsonDecode(frame.body!);
            print("location history----- ${jsonDecode(frame.body!)}");

            setState(() {
              for (var userId in widget.userIdsToTrack) {
                if (data.containsKey(userId)) {
                  // Clear existing path data for this user
                  _userPathPoints[userId]?.clear();
                  _userPathLines[userId]?.clear();
                  _userDistances[userId] = 0;

                  // Process historical data for this user
                  final locations = data[userId] as List<dynamic>;
                  for (final location in locations) {
                    final lat = location['latitude']?.toDouble() ?? 0.0;
                    final lng = location['longitude']?.toDouble() ?? 0.0;
                    final position = LatLng(lat, lng);
                    _userPathPoints[userId]?.add(position);

                    // Calculate distance if we have previous points
                    if (_userPathPoints[userId]!.length > 1) {
                      final prev = _userPathPoints[userId]![
                          _userPathPoints[userId]!.length - 2];
                      _userDistances[userId] = (_userDistances[userId] ?? 0) +
                          calculateDistance(prev.latitude, prev.longitude,
                              position.latitude, position.longitude);
                    }
                  }

                  // Set last position if we have points
                  if (_userPathPoints[userId]!.isNotEmpty) {
                    _userLastPositions[userId] = _userPathPoints[userId]!.last;
                  }

                  // Create polyline from all points
                  if (_userPathPoints[userId]!.isNotEmpty) {
                    _userPathLines[userId]?.add(Polyline(
                      polylineId: PolylineId('${userId}_historical_path'),
                      points: List<LatLng>.from(_userPathPoints[userId]!),
                      color: _userColors[userId]!,
                      width: 3,
                    ));
                  }
                }
              }

              // Move camera to show all paths if we have points
              var allPoints =
                  _userPathPoints.values.expand((points) => points).toList();
              if (allPoints.isNotEmpty) {
                _mapController?.animateCamera(CameraUpdate.newLatLngBounds(
                    _boundsFromLatLngList(allPoints), 50.0));
              }
            });
          } catch (e) {
            print('Error processing location history: $e');
          }
        }
      },
    );
  }

  void _subscribeToAlerts() {
    // // Subscribe to alerts for the requesting user
    // _stompClient.subscribe(
    //   destination: '/topic/alerts-${widget.userIdsToTrack.first}',
    //   callback: (frame) {
    //     if (frame.body != null) {
    //       try {
    //         final alert = jsonDecode(frame.body!);
    //         print("fgedtgdfyhfgtujhfgjghjhk $alert");
    //         _handleNewAlert(alert);
    //       } catch (e) {
    //         print('Error processing alert: $e');
    //       }
    //     }
    //   },
    // );
    
    // Also subscribe to alerts for each tracked user
    for (var userId in widget.userIdsToTrack) {
      print("idddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd: $userId");
      _stompClient.subscribe(
        destination: '/topic/alerts-$userId',
        
        callback: (frame) {
          if (frame.body != null) {
            try {
              final alert = jsonDecode(frame.body!);
                      print("fgedtgdfyhfgtujhfgjghjhk $alert");
              _handleNewAlert(alert);
            } catch (e) {
              print('Error processing alert: $e');
            }
          }
        },
      );
    }
  }

  // void _handleNewAlert(Map<String, dynamic> alert) {
  //   setState(() {
  //     _alerts.insert(0, alert); // Add new alert at beginning of list
  //     _showAlertPanel = true; // Show alert panel when new alert arrives
  //   });
    
  //   // Show a snackbar notification
  //   ScaffoldMessenger.of(context).showSnackBar(
  //     SnackBar(
  //       content: Text(alert['message'] ?? 'New alert received'),
  //       backgroundColor: Colors.red,
  //       duration: Duration(seconds: 5),
  //       action: SnackBarAction(
  //         label: 'View',
  //         textColor: Colors.white,
  //         onPressed: () {
  //           setState(() => _showAlertPanel = true);
  //         },
  //       ),
  //     ),
  //   );
    
  //   // Scroll to top when new alert arrives
  //   _alertController.animateTo(
  //     0,
  //     duration: Duration(milliseconds: 300),
  //     curve: Curves.easeOut,
  //   );
  // }


//   void _handleNewAlert(Map<String, dynamic> alert) {
//   // Convert the timestamp array to DateTime if it's in array format
//   DateTime? timestamp;
//   if (alert['timestamp'] is List) {
//     final tsList = alert['timestamp'] as List;
//     timestamp = DateTime(tsList[0], tsList[1], tsList[2], 
//                         tsList[3], tsList[4], tsList[5]);
//   } else if (alert['timestamp'] is String) {
//     timestamp = DateTime.tryParse(alert['timestamp']);
//   }

//   // Create a new alert with proper formatting
//   final formattedAlert = {
//     'message': alert['message'],
//     'timestamp': timestamp ?? DateTime.now(),
//     'userId': alert['userId'],
//     'isDanger': (alert['message'] as String).toLowerCase().contains('danger'),
//   };

//   setState(() {
//     _alerts.insert(0, formattedAlert);
//     _showAlertPanel = true;
//   });
  
//   ScaffoldMessenger.of(context).showSnackBar(
//     SnackBar(
//       content: Text(alert['message'] ?? 'New alert received'),
//       backgroundColor: formattedAlert['isDanger'] ? Colors.red : Colors.green,
//       duration: Duration(seconds: 5),
//       action: SnackBarAction(
//         label: 'View',
//         textColor: Colors.white,
//         onPressed: () {
//           setState(() => _showAlertPanel = true);
//         },
//       ),
//     ),
//   );
  
//   _alertController.animateTo(
//     0,
//     duration: Duration(milliseconds: 300),
//     curve: Curves.easeOut,
//   );
// }


//  // Modify your _handleNewAlert method to play sounds
//   void _handleNewAlert(Map<String, dynamic> alert) {
//     // Convert the timestamp array to DateTime if it's in array format
//     DateTime? timestamp;
//     if (alert['timestamp'] is List) {
//       final tsList = alert['timestamp'] as List;
//       timestamp = DateTime(tsList[0], tsList[1], tsList[2], 
//                           tsList[3], tsList[4], tsList[5]);
//     } else if (alert['timestamp'] is String) {
//       timestamp = DateTime.tryParse(alert['timestamp']);
//     }

//     // Determine if this is a danger alert
//     final isDanger = (alert['message'] as String).toLowerCase().contains('danger');

//     // Create a new alert with proper formatting
//     final formattedAlert = {
//       'message': alert['message'],
//       'timestamp': timestamp ?? DateTime.now(),
//       'userId': alert['userId'],
//       'isDanger': isDanger,
//     };

//     setState(() {
//       _alerts.insert(0, formattedAlert);
//       _showAlertPanel = true;
//     });

//     // Play appropriate sound based on alert type
//     _playAlertSound(isDanger);

//     // Show snackbar
//     ScaffoldMessenger.of(context).showSnackBar(
//       SnackBar(
//         content: Text(alert['message'] ?? 'New alert received'),
//         backgroundColor: isDanger ? Colors.red : Colors.green,
//         duration: Duration(seconds: 5),
//         action: SnackBarAction(
//           label: 'View',
//           textColor: Colors.white,
//           onPressed: () {
//             setState(() => _showAlertPanel = true);
//           },
//         ),
//       ),
//     );
    
//     _alertController.animateTo(
//       0,
//       duration: Duration(milliseconds: 300),
//       curve: Curves.easeOut,
//     );
//   }


 // Updated _handleNewAlert method
  void _handleNewAlert(Map<String, dynamic> alert) {
    final message = alert['message'] ?? 'Alert';
    final userId = alert['userId'] ?? 'user@gmail.com';
    final isDanger = message.toLowerCase().contains('danger');

    // Convert timestamp
    DateTime? timestamp;
    if (alert['timestamp'] is List) {
      final tsList = alert['timestamp'] as List;
      timestamp = DateTime(tsList[0], tsList[1], tsList[2], tsList[3], tsList[4], tsList[5]);
    }

    final formattedAlert = {
      'message': message,
      'timestamp': timestamp ?? DateTime.now(),
      'userId': userId,
      'isDanger': isDanger,
    };

    setState(() {
      _alerts.insert(0, formattedAlert);
      _showAlertPanel = true;
    });

    // Play voice alert with zone information
    _speakAlert(message, userId);
    
    // Play sound effect and vibration
    // _playAlertSound(isDanger);

    // Show snackbar
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isDanger ? Colors.red : Colors.green,
        duration: Duration(seconds: 5),
        action: SnackBarAction(
          label: 'View',
          textColor: Colors.white,
          onPressed: () => setState(() => _showAlertPanel = true),
        ),
      ),
    );
    
    _alertController.animateTo(
      0,
      duration: Duration(milliseconds: 300),
      curve: Curves.easeOut,
    );
  }


  Future<void> _playAlertSound(bool isDanger) async {
  if (_isPlayingAlertSound) return;
  
  try {
    _isPlayingAlertSound = true;
    
    // Vibrate for danger alerts
    if (isDanger && await Vibration.hasVibrator() ?? false) {
      Vibration.vibrate(duration: 500); // Vibrate for 500ms
    }
    
    final soundFile = isDanger ? 'danger_alert.mp3' : 'safe_alert.mp3';
    await _audioPlayer.play(AssetSource('sounds/$soundFile'));
    
    _audioPlayer.onPlayerComplete.listen((_) {
      _isPlayingAlertSound = false;
    });
  } catch (e) {
    print('Error playing sound: $e');
    _isPlayingAlertSound = false;
  }
}



// Future<void> _speakAlert(String message, String userId) async {
//   if (_isSpeaking) {
//     await _tts.stop();
//     await Future.delayed(Duration(milliseconds: 500));
//   }

//   try {
//     _isSpeaking = true;
    
//     // 1. Extract details
//     final nameMatch = RegExp(r'User\s(.+?)\s').firstMatch(message);
//     final userName = nameMatch?.group(1) ?? 'User';
//     final isDanger = message.toLowerCase().contains('danger');
//     final zone = _extractZoneName(message);

//     // 2. Configure TTS for clear Indian English
//     await _tts.setLanguage("en-IN");
//     await _tts.setSpeechRate(0.45); // Slower for clarity
//     await _tts.setVolume(1.0);
//     await _tts.setPitch(1.0);
    
//     // 3. Build message with pauses
//     final alertMessage = isDanger
//         ? "Hey. $userName. has Entered danger zone. $zone."
//         : "Hey. $userName. has Left safe zone. $zone.";

//     // 4. Speak 3 times with pauses
//     for (int i = 0; i < 3; i++) {
//       await _tts.speak(alertMessage);
//       await _tts.awaitSpeakCompletion(true);
//       if (i < 2) await Future.delayed(Duration(milliseconds: 800));
//     }
    
//   } catch (e) {
//     debugPrint('TTS error: $e');
//     await _tts.speak("New alert received");
//   } finally {
//     _isSpeaking = false;
//   }
// }


Future<void> _speakAlert(String message, String userId) async {
  if (_isSpeaking) {
    await _tts.stop();
    await Future.delayed(Duration(milliseconds: 500));
  }

  try {
    _isSpeaking = true;
    
    // 1. Extract details
    final nameMatch = RegExp(r'User\s(.+?)\s').firstMatch(message);
    final userName = nameMatch?.group(1) ?? 'User';
    final isDanger = message.toLowerCase().contains('danger');
    final zone = _extractZoneName(message);

    // 2. Configure TTS for clear Indian English
    await _tts.setLanguage("en-IN");
    await _tts.setSpeechRate(0.45);
    await _tts.setVolume(1.0);
    await _tts.setPitch(1.0);
    
    // 3. Build message
    final alertMessage = isDanger
        ? "Hey. $userName. has Entered danger zone. $zone."
        : "Hey. $userName. has Left safe zone. $zone.";

    // 4. Speak and vibrate 3 times
    for (int i = 0; i < 3; i++) {
      // Start vibration pattern
      if (await Vibration.hasVibrator() ?? false) {
        Vibration.vibrate(pattern: [500, 500]); // Vibrate for 500ms, pause 500ms
      }
      
      // Speak alert
      await _tts.speak(alertMessage);
      await _tts.awaitSpeakCompletion(true);
      
      // Pause between repetitions
      if (i < 2) {
        await Vibration.cancel(); // Stop vibration during pause
        await Future.delayed(Duration(milliseconds: 800));
      }
    }
    
  } catch (e) {
    debugPrint('Alert error: $e');
    await _tts.speak("New alert received");
  } finally {
    await Vibration.cancel(); // Ensure vibration stops
    _isSpeaking = false;
  }
}
String _extractZoneName(String message) {
  final zoneMatch = RegExp(r"zone\s'(.+?)'|zone\s(.+?)\s\d").firstMatch(message);
  return zoneMatch?.group(1) ?? zoneMatch?.group(2) ?? 'the area';
}




  
//  Future<void> _playAlertSound(bool isDanger) async {
//     if (_isPlayingAlertSound) return;

//     try {
//       _isPlayingAlertSound = true;
      
//       // Stop any currently playing sound
//       await _audioPlayer.stop();
      
//       // Configure audio settings
//       await _audioPlayer.setVolume(1.0);
//       await _audioPlayer.setReleaseMode(ReleaseMode.release);

//       // Play the sound directly from assets root
//       final soundFile = isDanger ? 'danger_alert.mp3' : 'safe_alert.mp3';
//       await _audioPlayer.play(
//         AssetSource(soundFile),  // Removed 'sounds/' prefix
//         volume: 1.0,
//         mode: PlayerMode.mediaPlayer,
//       );

//       // Reset flag when complete
//       _audioPlayer.onPlayerComplete.listen((_) {
//         _isPlayingAlertSound = false;
//       });

//     } catch (e) {
//       debugPrint('Sound error: $e');
//       _isPlayingAlertSound = false;
//     }
//   }

  LatLngBounds _boundsFromLatLngList(List<LatLng> list) {
    double? x0, x1, y0, y1;
    for (LatLng latLng in list) {
      if (x0 == null) {
        x0 = x1 = latLng.latitude;
        y0 = y1 = latLng.longitude;
      } else {
        if (latLng.latitude > x1!) x1 = latLng.latitude;
        if (latLng.latitude < x0) x0 = latLng.latitude;
        if (latLng.longitude > y1!) y1 = latLng.longitude;
        if (latLng.longitude < y0!) y0 = latLng.longitude;
      }
    }
    return LatLngBounds(
      northeast: LatLng(x1!, y1!),
      southwest: LatLng(x0!, y0!),
    );
  }

  void _updatePath(String userId, LatLng newPosition) {
    _userPathPoints[userId]?.add(newPosition);

    if (_userPathPoints[userId]!.length > 1) {
      final prev =
          _userPathPoints[userId]![_userPathPoints[userId]!.length - 2];
      final distance = calculateDistance(prev.latitude, prev.longitude,
          newPosition.latitude, newPosition.longitude);
      _userDistances[userId] = (_userDistances[userId] ?? 0) + distance;
    }

    _userPathLines[userId]?.clear();
    if (_userPathPoints[userId]!.length > 1) {
      _userPathLines[userId]?.add(Polyline(
        polylineId: PolylineId('${userId}_path'),
        points: List<LatLng>.from(_userPathPoints[userId]!),
        color: _userColors[userId]!,
        width: 3,
      ));
    }
  }

  double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    const R = 6371e3;
    final phi1 = lat1 * pi / 180;
    final phi2 = lat2 * pi / 180;
    final deltaPhi = (lat2 - lat1) * pi / 180;
    final deltaLambda = (lon2 - lon1) * pi / 180;

    final a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
        cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2);
    final c = 2 * atan2(sqrt(a), sqrt(1 - a));

    return R * c;
  }

  void _sendTrackingRequests() {
    for (var userId in widget.userIdsToTrack) {
      _stompClient.send(
        destination: '/app/subscribe-to-user',
        body: jsonEncode({
          'requestingUserId': widget.requestingUserId,
          'userIdToTrack': userId,
        }),
      );
    }
  }

  void _updateMap(String userId, LatLng position) {
    final markerId = MarkerId(userId);

    setState(() {
      _markers.removeWhere((marker) => marker.markerId == markerId);
      _markers.add(Marker(
        markerId: markerId,
        position: position,
        icon: BitmapDescriptor.defaultMarkerWithHue(
          _getHueForColor(_userColors[userId]!),
        ),
        infoWindow: InfoWindow(
          title: 'User ${widget.userIdsToTrack.indexOf(userId) + 1}',
          snippet:
              '${position.latitude.toStringAsFixed(6)}, ${position.longitude.toStringAsFixed(6)}',
        ),
      ));
    });
  }

  void _addFootprint(String userId, LatLng position) {
    setState(() {
      _footprints.add(Circle(
        circleId: CircleId('${userId}_${Uuid().v4()}'),
        center: position,
        radius: 5,
        strokeWidth: 0,
        fillColor: _userColors[userId]!.withOpacity(0.7),
      ));
    });
  }

  void _moveToUserLocation(String userId) {
    final position = _userLastPositions[userId];
    if (position != null && _mapController != null) {
      _mapController.animateCamera(
        CameraUpdate.newLatLngZoom(position, 15),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('No location data available for this user')),
      );
    }
  }

  double _getHueForColor(Color color) {
    final hsl = HSLColor.fromColor(color);
    return hsl.hue;
  }

  Widget _buildInfoBox(IconData icon, String title, String value) {
    return Container(
      width: 150,
      padding: EdgeInsets.all(12),
      margin: EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12,
            blurRadius: 4,
            offset: Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, color: Colors.blue[800], size: 24),
          SizedBox(height: 8),
          Text(title, style: TextStyle(fontSize: 12, color: Colors.grey[600])),
          SizedBox(height: 4),
          Text(value,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.blue[800],
              )),
        ],
      ),
    );
  }

  Widget _buildUserStatusBox(int userIndex) {
    final userId = widget.userIdsToTrack[userIndex];
    final color = _userColors[userId]!;
    final timeFormat = DateFormat('hh:mm:ss a');

    return Container(
      width: 180,
      padding: EdgeInsets.all(12),
      margin: EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12,
            blurRadius: 4,
            offset: Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: color,
              shape: BoxShape.circle,
            ),
          ),
          SizedBox(height: 8),
          Text('User ${userIndex + 1}',
              style: TextStyle(fontSize: 12, color: Colors.grey[600])),
          SizedBox(height: 4),
          Text(
            _userLastUpdateTimes[userId] != null
                ? timeFormat.format(_userLastUpdateTimes[userId]!)
                : '--:--:--',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          SizedBox(height: 4),
          Text(
            '${((_userDistances[userId] ?? 0) / 1000).toStringAsFixed(2)} km',
            style: TextStyle(
              fontSize: 14,
              color: color,
            ),
          ),
          SizedBox(height: 8),
          ElevatedButton(
            onPressed: () => _moveToUserLocation(userId),
            child: Text("View Location"),
            style: ElevatedButton.styleFrom(
              backgroundColor: color,
              padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              textStyle: TextStyle(fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }

  // Widget _buildAlertPanel() {
  //   if (!_showAlertPanel || _alerts.isEmpty) return SizedBox.shrink();
    
  //   return Positioned(
  //     bottom: MediaQuery.of(context).size.height * 0.25 + 20,
  //     left: 20,
  //     right: 20,
  //     child: Material(
  //       elevation: 8,
  //       borderRadius: BorderRadius.circular(12),
  //       child: Container(
  //         padding: EdgeInsets.all(12),
  //         decoration: BoxDecoration(
  //           color: Colors.white,
  //           borderRadius: BorderRadius.circular(12),
  //           boxShadow: [
  //             BoxShadow(
  //               color: Colors.black26,
  //               blurRadius: 10,
  //               offset: Offset(0, 5),
  //             ),
  //           ],
  //         ),
  //         child: Column(
  //           crossAxisAlignment: CrossAxisAlignment.start,
  //           children: [
  //             Row(
  //               mainAxisAlignment: MainAxisAlignment.spaceBetween,
  //               children: [
  //                 Row(
  //                   children: [
  //                     Icon(Icons.warning, color: Colors.red),
  //                     SizedBox(width: 8),
  //                     Text('Alerts', style: TextStyle(
  //                       fontWeight: FontWeight.bold, 
  //                       fontSize: 18,
  //                       color: Colors.red,
  //                     )),
  //                   ],
  //                 ),
  //                 IconButton(
  //                   icon: Icon(Icons.close),
  //                   onPressed: () => setState(() => _showAlertPanel = false),
  //                 ),
  //               ],
  //             ),
  //             Divider(),
  //             ConstrainedBox(
  //               constraints: BoxConstraints(maxHeight: 200),
  //               child: _alerts.isEmpty
  //                   ? Center(child: Text('No alerts received'))
  //                   : ListView.builder(
  //                       controller: _alertController,
  //                       shrinkWrap: true,
  //                       itemCount: _alerts.length,
  //                       itemBuilder: (context, index) {
  //                         final alert = _alerts[index];
  //                         return Card(
  //                           margin: EdgeInsets.symmetric(vertical: 4),
  //                           child: ListTile(
  //                             leading: Icon(Icons.warning, color: Colors.orange),
  //                             title: Text(
  //                               alert['message'] ?? 'Alert',
  //                               style: TextStyle(fontWeight: FontWeight.bold),
  //                             ),
  //                             subtitle: Text(
  //                               alert['timestamp'] != null 
  //                                   ? DateFormat('MMM d, hh:mm a').format(
  //                                       DateTime.parse(alert['timestamp']).toLocal())
  //                                   : 'Just now',
  //                             ),
  //                             trailing: IconButton(
  //                               icon: Icon(Icons.location_on),
  //                               onPressed: () {
  //                                 // TODO: Implement zoom to alert location if available
  //                               },
  //                             ),
  //                           ),
  //                         );
  //                       },
  //                     ),
  //             ),
  //             if (_alerts.isNotEmpty)
  //               TextButton(
  //                 onPressed: () => setState(() => _alerts.clear()),
  //                 child: Text('Clear All Alerts'),
  //               ),
  //           ],
  //         ),
  //       ),
  //     ),
  //   );
  // }

  Widget _buildAlertPanel() {
  if (!_showAlertPanel || _alerts.isEmpty) return SizedBox.shrink();
  
  return Positioned(
    bottom: MediaQuery.of(context).size.height * 0.25 + 20,
    left: 20,
    right: 20,
    child: Material(
      elevation: 8,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.black26,
              blurRadius: 10,
              offset: Offset(0, 5),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Icon(Icons.warning, color: Colors.red),
                    SizedBox(width: 8),
                    Text('Alerts', style: TextStyle(
                      fontWeight: FontWeight.bold, 
                      fontSize: 18,
                      color: Colors.red,
                    )),
                  ],
                ),
                IconButton(
                  icon: Icon(Icons.close),
                  onPressed: () => setState(() => _showAlertPanel = false),
                ),
              ],
            ),
            Divider(),
            ConstrainedBox(
              constraints: BoxConstraints(maxHeight: 200),
              child: _alerts.isEmpty
                  ? Center(child: Text('No alerts received'))
                  : ListView.builder(
                      controller: _alertController,
                      shrinkWrap: true,
                      itemCount: _alerts.length,
                      itemBuilder: (context, index) {
                        final alert = _alerts[index];
                        final isDanger = alert['isDanger'] ?? false;
                        
                        return Card(
                          margin: EdgeInsets.symmetric(vertical: 4),
                          color: isDanger ? Colors.red[50] : Colors.green[50],
                          child: ListTile(
                            leading: Icon(
                              Icons.warning,
                              color: isDanger ? Colors.red : Colors.green,
                            ),
                            title: Text(
                              alert['message'] ?? 'Alert',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: isDanger ? Colors.red[800] : Colors.green[800],
                              ),
                            ),
                            subtitle: Text(
                              DateFormat('MMM d, hh:mm a').format(
                                alert['timestamp'] is DateTime 
                                  ? (alert['timestamp'] as DateTime).toLocal()
                                  : DateTime.now().toLocal()
                              ),
                              style: TextStyle(
                                color: isDanger ? Colors.red[600] : Colors.green[600],
                              ),
                            ),
                            trailing: IconButton(
                              icon: Icon(Icons.location_on),
                              color: isDanger ? Colors.red : Colors.green,
                              onPressed: () {
                                // TODO: Implement zoom to alert location if available
                              },
                            ),
                          ),
                        );
                      },
                    ),
            ),
            if (_alerts.isNotEmpty)
              TextButton(
                onPressed: () => setState(() => _alerts.clear()),
                child: Text('Clear All Alerts'),
              ),
          ],
        ),
      ),
    ),
  );
}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          "Live Location Tracking",
          style: TextStyle(
            color: Colors.black54,
            fontSize: 20,
            fontWeight: FontWeight.w400,
          ),
        ),
        actions: [
          // Stack(
          //   children: [
          //     IconButton(
          //       splashRadius: 20,
          //       icon: const Icon(Icons.notifications_active, color: Colors.black54),
          //       onPressed: () {
          //         setState(() => _showAlertPanel = !_showAlertPanel);
          //       },
          //     ),
          //     if (_alerts.isNotEmpty)
          //       Positioned(
          //         right: 8,
          //         top: 8,
          //         child: Container(
          //           padding: EdgeInsets.all(4),
          //           decoration: BoxDecoration(
          //             color: Colors.red,
          //             shape: BoxShape.circle,
          //           ),
          //           child: Text(
          //             '${_alerts.length}',
          //             style: TextStyle(color: Colors.white, fontSize: 12),
          //           ),
          //         ),
          //       ),
          //   ],
          // ),
          Stack(
  children: [
    IconButton(
      splashRadius: 20,
      icon: const Icon(Icons.notifications_active, color: Colors.black54),
      onPressed: () {
        setState(() => _showAlertPanel = !_showAlertPanel);
      },
    ),
    if (_alerts.isNotEmpty)
      Positioned(
        right: 8,
        top: 8,
        child: Container(
          padding: EdgeInsets.all(4),
          decoration: BoxDecoration(
            color: _alerts.any((alert) => alert['isDanger'] == true) 
              ? Colors.red 
              : Colors.green,
            shape: BoxShape.circle,
          ),
          child: Text(
            '${_alerts.length}',
            style: TextStyle(color: Colors.white, fontSize: 12),
          ),
        ),
      ),
  ],
),
        ],
      ),
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                flex: 3,
                child: Stack(
                  children: [
                    GoogleMap(
                      onMapCreated: (controller) => _mapController = controller,
                      initialCameraPosition: CameraPosition(
                        target: LatLng(28.5892, 77.3176),
                        zoom: 15,
                      ),
                      markers: _markers,
                      circles: _footprints,
                      polylines: _userPathLines.values.expand((lines) => lines).toSet(),
                    ),
                    Positioned(
                      top: 10,
                      right: 10,
                      child: Container(
                        padding: EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: _isConnected ? Colors.green : Colors.red,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          _isConnected ? 'Connected' : 'Disconnected',
                          style: TextStyle(color: Colors.white),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                height: MediaQuery.of(context).size.height * 0.25,
                padding: EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(
                  color: Colors.grey[100],
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(20),
                    topRight: Radius.circular(20),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black12,
                      blurRadius: 10,
                      offset: Offset(0, -5),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    Container(
                      width: 40,
                      height: 4,
                      margin: EdgeInsets.only(bottom: 8),
                      decoration: BoxDecoration(
                        color: Colors.grey[400],
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                    Expanded(
                      child: SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            SizedBox(width: 8),
                            _buildInfoBox(
                              Icons.calendar_today,
                              'Tracking Since',
                              _trackingStartTime != null
                                  ? DateFormat('MMM d, y')
                                      .format(_trackingStartTime!)
                                  : '--/--/----',
                            ),
                            ...List.generate(widget.userIdsToTrack.length,
                                (index) => _buildUserStatusBox(index)),
                            SizedBox(width: 8),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          _buildAlertPanel(),
        ],
      ),
    );
  }
}