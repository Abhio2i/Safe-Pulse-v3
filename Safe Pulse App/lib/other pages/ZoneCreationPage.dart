import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:location/location.dart';
import 'package:Safe_pulse/other%20pages/ZoneHistoryPage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;
import 'package:Safe_pulse/Api/ApiService.dart';

class ZoneCreationPage extends StatefulWidget {
  @override
  _ZoneCreationPageState createState() => _ZoneCreationPageState();
}

class _ZoneCreationPageState extends State<ZoneCreationPage> {
  late GoogleMapController mapController;
  final Location _location = Location();
  LatLng? _currentLocation;
  LatLng? _selectedLocation;
  double _radius = 100.0;
  String _zoneType = 'SAFE';
  bool _isLoading = true;
  bool _locationServiceEnabled = false;
  final TextEditingController _nameController = TextEditingController();
  Set<Marker> _markers = {};
  Set<Circle> _circles = {};

  // User connection variables
  List<dynamic> _activeConnections = [];
  List<String> _selectedUserEmails = [];
  String? _userEmail;
  String _errorMessage = "";

  // SOS options
  bool _enableSOSAlert = true;
  bool _enableCall = true;
  bool _enableNotification = true;
  bool _enableMessage = true;

  // Panel control
  bool _showDetailsPanel = true;
  final _panelScrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _loadUserData();
    _checkLocationPermission();
  }

  Future<void> _loadUserData() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _userEmail = prefs.getString("username");
    });

    if (_userEmail != null) {
      _fetchActiveConnections();
    } else {
      setState(() {
        _errorMessage = "User email not found";
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchActiveConnections() async {
    if (_userEmail == null) return;

    setState(() {
      _isLoading = true;
      _errorMessage = "";
    });

    try {
      final response = await http.get(Uri.parse(
          '${ApiService.baseUrl}/api/relationships/getUserRelations?email=$_userEmail'));

      if (response.statusCode == 200) {
        print("gdfgdhgdfhjfdjhfd ${response.body}");
        final data = json.decode(response.body);
        setState(() {
          _activeConnections = data
              .where((relation) =>
                  (relation['activityStatus'] == 'Not Active' ||
                      relation['activityStatus'] == null) &&
                  relation['relationDirection'] == 'connected')
              .toList();
          print("gdfgdhgdfhjfdjhfd ${_activeConnections}");
        });
      } else {
        setState(() {
          _errorMessage =
              "Failed to fetch connections. Status code: ${response.statusCode}";
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = "Error fetching connections: $e";
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _checkLocationPermission() async {
    try {
      _locationServiceEnabled = await _location.serviceEnabled();
      if (!_locationServiceEnabled) {
        _locationServiceEnabled = await _location.requestService();
        if (!_locationServiceEnabled)
          throw Exception('Location services disabled');
      }

      var permissionStatus = await _location.hasPermission();
      if (permissionStatus == PermissionStatus.denied) {
        permissionStatus = await _location.requestPermission();
        if (permissionStatus != PermissionStatus.granted) {
          throw Exception('Location permissions denied');
        }
      }

      final locationData = await _location.getLocation();
      setState(() {
        _currentLocation =
            LatLng(locationData.latitude!, locationData.longitude!);
        _selectedLocation = _currentLocation;
        _updateMarkerAndCircle();
      });

      _location.onLocationChanged.listen((LocationData currentLocation) {
        if (mounted) {
          setState(() {
            _currentLocation =
                LatLng(currentLocation.latitude!, currentLocation.longitude!);
          });
        }
      });
    } catch (e) {
      setState(() {
        _errorMessage = "Location error: $e";
        _isLoading = false;
      });
    }
  }

  void _updateMarkerAndCircle() {
    if (_selectedLocation == null) return;

    setState(() {
      _markers = {
        Marker(
          markerId: MarkerId('selected-location'),
          position: _selectedLocation!,
          infoWindow: InfoWindow(title: 'Selected Location'),
          icon: BitmapDescriptor.defaultMarkerWithHue(
            _zoneType == 'SAFE'
                ? BitmapDescriptor.hueGreen
                : BitmapDescriptor.hueRed,
          ),
        ),
      };

      _circles = {
        Circle(
          circleId: CircleId('zone-radius'),
          center: _selectedLocation!,
          radius: _radius,
          strokeWidth: 2,
          strokeColor: _zoneType == 'SAFE' ? Colors.green : Colors.red,
          fillColor: _zoneType == 'SAFE'
              ? Colors.green.withOpacity(0.2)
              : Colors.red.withOpacity(0.2),
        ),
      };
    });
  }

  void _onMapCreated(GoogleMapController controller) {
    mapController = controller;
    if (_selectedLocation != null) {
      mapController
          .animateCamera(CameraUpdate.newLatLngZoom(_selectedLocation!, 15));
    }
  }

  void _onMapTapped(LatLng location) {
    setState(() {
      _selectedLocation = location;
      _updateMarkerAndCircle();
    });
  }

  Future<void> _saveZone() async {
    if (_selectedLocation == null || _nameController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please select location and provide zone name')),
      );
      return;
    }

    if (_selectedUserEmails.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please select at least one user')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      final zoneData = {
        'name': _nameController.text,
        'latitude': _selectedLocation!.latitude,
        'longitude': _selectedLocation!.longitude,
        'radius': _radius,
        'type': _zoneType, // Make sure it's "SAFE" or "DANGER"
        'createdBy': _userEmail,
        'sharedWith': _selectedUserEmails,
        'sosSettings': {
          'alert': _enableSOSAlert,
          'call': _enableCall,
          'notification': _enableNotification,
          'message': _enableMessage,
        }
      };

      print("Creating zone with data: ${jsonEncode(zoneData)}");

      await ApiService().createSafeZone(zoneData);

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Zone created successfully!')),
      );

      // Navigator.pop(context, true);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error creating zone: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  // Future<void> _saveZone() async {
  //   if (_selectedLocation == null || _nameController.text.isEmpty) {
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       SnackBar(content: Text('Please select location and provide zone name')),
  //     );
  //     return;
  //   }

  //   if (_selectedUserEmails.isEmpty) {
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       SnackBar(content: Text('Please select at least one user')),
  //     );
  //     return;
  //   }

  //   setState(() => _isLoading = true);
  //   try {
  //     print("asfvcsdfgsdgdsfhdshdsf ${json.encode({
  //           'name': _nameController.text,
  //           'latitude': _selectedLocation!.latitude,
  //           'longitude': _selectedLocation!.longitude,
  //           'radius': _radius,
  //           'type': _zoneType,
  //           'created_by': _userEmail,
  //           'shared_with': _selectedUserEmails,
  //           'sos_settings': {
  //             'alert': _enableSOSAlert,
  //             'call': _enableCall,
  //             'notification': _enableNotification,
  //             'message': _enableMessage,
  //           },
  //         })}");
  //     final response = await http.post(
  //       Uri.parse('${ApiService.baseUrl}/api/zones/create'),
  //       headers: {'Content-Type': 'application/json'},
  //       body: json.encode({
  //         'name': _nameController.text,
  //         'latitude': _selectedLocation!.latitude,
  //         'longitude': _selectedLocation!.longitude,
  //         'radius': _radius,
  //         'type': _zoneType,
  //         'created_by': _userEmail,
  //         'shared_with': _selectedUserEmails,
  //         'sos_settings': {
  //           'alert': _enableSOSAlert,
  //           'call': _enableCall,
  //           'notification': _enableNotification,
  //           'message': _enableMessage,
  //         },
  //       }),
  //     );

  //     if (response.statusCode == 200) {
  //       ScaffoldMessenger.of(context).showSnackBar(
  //         SnackBar(content: Text('Zone created successfully!')),
  //       );
  //       Navigator.pop(context, true);
  //     } else {
  //       throw Exception('Failed to create zone: ${response.statusCode}');
  //     }
  //   } catch (e) {
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       SnackBar(content: Text('Error creating zone: $e')),
  //     );
  //   } finally {
  //     setState(() => _isLoading = false);
  //   }
  // }

  Widget _buildUserSelection() {
    return ExpansionTile(
      title: Text('Select Users (${_selectedUserEmails.length} selected)'),
      children: _activeConnections.map((connection) {
        final email = connection['otherUserEmail'].toString();
        final name = connection['relationName'].toString();
        return CheckboxListTile(
          title: Text('$name ($email)'),
          value: _selectedUserEmails.contains(email),
          onChanged: (bool? value) {
            setState(() {
              if (value == true) {
                _selectedUserEmails.add(email);
              } else {
                _selectedUserEmails.remove(email);
              }
            });
          },
        );
      }).toList(),
    );
  }

  Widget _buildSOSOptions() {
    return ExpansionTile(
      title: Text('SOS Alert Options'),
      children: [
        CheckboxListTile(
          title: Text('Enable SOS Alert'),
          value: _enableSOSAlert,
          onChanged: (bool? value) =>
              setState(() => _enableSOSAlert = value ?? false),
        ),
        CheckboxListTile(
          title: Text('Enable Emergency Call'),
          value: _enableCall,
          onChanged: (bool? value) =>
              setState(() => _enableCall = value ?? false),
        ),
        CheckboxListTile(
          title: Text('Enable Notification'),
          value: _enableNotification,
          onChanged: (bool? value) =>
              setState(() => _enableNotification = value ?? false),
        ),
        CheckboxListTile(
          title: Text('Enable Message'),
          value: _enableMessage,
          onChanged: (bool? value) =>
              setState(() => _enableMessage = value ?? false),
        ),
      ],
    );
  }

  Widget _buildDetailsPanel() {
    return AnimatedPositioned(
      duration: Duration(milliseconds: 300),
      curve: Curves.easeInOut,
      bottom: _showDetailsPanel ? 10 : -400,
      left: 10,
      right: 10,
      child: GestureDetector(
        onVerticalDragUpdate: (details) {
          if (details.primaryDelta! > 5) {
            setState(() => _showDetailsPanel = false);
          } else if (details.primaryDelta! < -5) {
            setState(() => _showDetailsPanel = true);
          }
        },
        child: Card(
          elevation: 8,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          child: Container(
            height: 400,
            padding: EdgeInsets.all(15),
            child: Column(
              children: [
                Icon(Icons.drag_handle),
                Expanded(
                  child: Scrollbar(
                    controller: _panelScrollController,
                    child: SingleChildScrollView(
                      controller: _panelScrollController,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Zone Details',
                              style: TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.bold)),
                          SizedBox(height: 10),
                          TextField(
                            controller: _nameController,
                            decoration: InputDecoration(
                              labelText: 'Zone Name',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          SizedBox(height: 15),
                          Text('Zone Radius: ${_radius.round()} meters'),
                          Slider(
                            value: _radius,
                            min: 50,
                            max: 1000,
                            divisions: 19,
                            onChanged: (value) {
                              setState(() {
                                _radius = value;
                                _updateMarkerAndCircle();
                              });
                            },
                          ),
                          SizedBox(height: 10),
                          Text('Zone Type',
                              style: TextStyle(color: Colors.grey[600])),
                          Row(
                            children: [
                              Expanded(
                                child: ChoiceChip(
                                  label: Text('Safe Zone'),
                                  selected: _zoneType == 'safe',
                                  selectedColor: Colors.green,
                                  onSelected: (selected) => setState(() {
                                    _zoneType = 'SAFE';
                                    _updateMarkerAndCircle();
                                  }),
                                ),
                              ),
                              SizedBox(width: 10),
                              Expanded(
                                child: ChoiceChip(
                                  label: Text('Danger Zone'),
                                  selected: _zoneType == 'DANGER',
                                  selectedColor: Colors.red,
                                  onSelected: (selected) => setState(() {
                                    _zoneType = 'DANGER';
                                    _updateMarkerAndCircle();
                                  }),
                                ),
                              ),
                            ],
                          ),
                          SizedBox(height: 15),
                          _buildUserSelection(),
                          SizedBox(height: 10),
                          _buildSOSOptions(),
                          SizedBox(height: 15),
                          ElevatedButton(
                            onPressed: _saveZone,
                            child: Text('Save Zone'),
                            style: ElevatedButton.styleFrom(
                              minimumSize: Size(double.infinity, 50),
                              backgroundColor: Color(0xFF5C6BC0),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Create Safety Zone'),
        backgroundColor: Color(0xFF5C6BC0),
        actions: [
          IconButton(
            icon: Icon(Icons.my_location),
            onPressed: () {
              if (_currentLocation != null) {
                setState(() {
                  _selectedLocation = _currentLocation;
                  _updateMarkerAndCircle();
                });
                mapController.animateCamera(
                  CameraUpdate.newLatLngZoom(_currentLocation!, 15),
                );
              }
            },
          ),
          IconButton(
            icon: Icon(Icons.history),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => ZoneHistoryPage()),
              );
            },
          ),
        ],
      ),
      body: Stack(
        children: [
          if (_selectedLocation != null)
            GoogleMap(
              onMapCreated: _onMapCreated,
              initialCameraPosition: CameraPosition(
                target: _selectedLocation!,
                zoom: 15,
              ),
              onTap: _onMapTapped,
              markers: _markers,
              circles: _circles,
              myLocationEnabled: true,
            )
          else
            Center(child: CircularProgressIndicator()),
          if (!_showDetailsPanel)
            Positioned(
              bottom: 10,
              left: 0,
              right: 0,
              child: Center(
                child: GestureDetector(
                  onTap: () => setState(() => _showDetailsPanel = true),
                  child: Container(
                    padding: EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(color: Colors.black26, blurRadius: 5)
                      ],
                    ),
                    child: Icon(Icons.keyboard_arrow_up, size: 30),
                  ),
                ),
              ),
            ),
          _buildDetailsPanel(),
          if (_isLoading) Center(child: CircularProgressIndicator()),
          if (_errorMessage.isNotEmpty)
            Positioned(
              top: 20,
              left: 20,
              right: 20,
              child: Container(
                padding: EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.red[100],
                  borderRadius: BorderRadius.circular(5),
                ),
                child: Row(
                  children: [
                    Icon(Icons.error, color: Colors.red),
                    SizedBox(width: 10),
                    Expanded(child: Text(_errorMessage)),
                    IconButton(
                      icon: Icon(Icons.close),
                      onPressed: () => setState(() => _errorMessage = ""),
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _panelScrollController.dispose();
    super.dispose();
  }
}
