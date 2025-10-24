import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_phone_direct_caller/flutter_phone_direct_caller.dart';
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

class ContactPage extends StatefulWidget {
  const ContactPage({Key? key}) : super(key: key);

  @override
  _ContactPageState createState() => _ContactPageState();
}

class _ContactPageState extends State<ContactPage> {
  final ApiService _apiService = ApiService();
  List<Map<String, dynamic>> contacts = [];
  Map<String, String> emergencyNumbers = {
    'Ambulance': '102',
    'Police': '100',
    'Fire': '101',
    'Emergency': '112',
  };
  bool _isLoading = true;
  String? userId;

  @override
  void initState() {
    super.initState();
    _loadUserId();
  }

  Future<void> _loadUserId() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final savedUserId = prefs.getString("userId");

      if (savedUserId != null) {
        setState(() {
          userId = savedUserId;
        });
        await _loadData();
      } else {
        setState(() {
          _isLoading = false;
        });
        _showErrorSnackBar('User ID not found');
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      _showErrorSnackBar('Failed to load user ID');
    }
  }

  Future<void> _loadData() async {
    try {
      if (userId == null) return;

      setState(() {
        _isLoading = true;
      });

      // Load emergency contacts
      final emergencyContacts = await _apiService.getEmergencyContacts(userId!);
      setState(() {
        emergencyNumbers = {
          'Ambulance': emergencyContacts['ambulance'] ?? '102',
          'Police': emergencyContacts['police'] ?? '100',
          'Fire': emergencyContacts['fire'] ?? '101',
          'Emergency': emergencyContacts['emergency'] ?? '112',
        };
      });

      // Load personal contacts
      final personalContacts = await _apiService.getMyContacts(userId: userId!);
      setState(() {
        contacts = personalContacts.map((contact) {
          return {
            'name': contact['name'] ?? 'Unknown',
            'phone': contact['number'] ?? '',
            'email': '',
            'image':
                'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=200&auto=format&fit=crop',
            'relation': contact['relation'] ?? 'Contact',
            'address': ''
          };
        }).toList();
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading data: $e');
      setState(() {
        _isLoading = false;
      });
      _showErrorSnackBar('Failed to load contacts');
    }
  }

  void _showErrorSnackBar(String message) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );
    });
  }

  Future<void> _showEditEmergencyNumbersDialog() async {
    final ambulanceController =
        TextEditingController(text: emergencyNumbers['Ambulance']);
    final policeController =
        TextEditingController(text: emergencyNumbers['Police']);
    final fireController =
        TextEditingController(text: emergencyNumbers['Fire']);
    final emergencyController =
        TextEditingController(text: emergencyNumbers['Emergency']);

    await showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text('Edit Emergency Numbers'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _buildNumberEditField('Ambulance', ambulanceController,
                    Icons.local_hospital, Colors.red),
                _buildNumberEditField('Police', policeController,
                    Icons.local_police, Colors.blueGrey),
                _buildNumberEditField(
                    'Fire', fireController, Icons.fire_truck, Colors.orange),
                _buildNumberEditField('Emergency', emergencyController,
                    Icons.call, Colors.purple),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                try {
                  await _apiService.updateEmergencyContacts(
                    userId: userId!,
                    ambulance: ambulanceController.text,
                    police: policeController.text,
                    fire: fireController.text,
                    emergency: emergencyController.text,
                  );

                  setState(() {
                    emergencyNumbers['Ambulance'] = ambulanceController.text;
                    emergencyNumbers['Police'] = policeController.text;
                    emergencyNumbers['Fire'] = fireController.text;
                    emergencyNumbers['Emergency'] = emergencyController.text;
                  });

                  Navigator.pop(context);
                  _showErrorSnackBar('Emergency contacts updated successfully');
                } catch (e) {
                  Navigator.pop(context);
                  _showErrorSnackBar('Failed to update emergency contacts');
                }
              },
              child: Text('Save'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Color(0xFF5C6BC0),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildNumberEditField(String label, TextEditingController controller,
      IconData icon, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextField(
        controller: controller,
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: Icon(icon, color: color),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
        keyboardType: TextInputType.phone,
      ),
    );
  }

  Widget _buildQuickActionButton({
    required IconData icon,
    required String label,
    required Color color,
    required VoidCallback onPressed,
  }) {
    return Column(
      children: [
        Container(
          width: 60,
          height: 60,
          decoration: BoxDecoration(
            color: color.withOpacity(0.2),
            shape: BoxShape.circle,
          ),
          child: IconButton(
            icon: Icon(icon, color: color, size: 30),
            onPressed: onPressed,
          ),
        ),
        SizedBox(height: 5),
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w500,
            color: Colors.grey[700],
          ),
        ),
      ],
    );
  }

  Widget _buildContactCard(BuildContext context, Map<String, dynamic> contact) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 15, vertical: 8),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(15),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(15),
        onTap: () {
          _showContactDetails(context, contact);
        },
        child: Padding(
          padding: EdgeInsets.all(12),
          child: Row(
            children: [
              Container(
                width: 50,
                height: 50,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: _getRelationColor(contact['relation']!),
                    width: 2,
                  ),
                ),
                child: ClipOval(
                  child: CachedNetworkImage(
                    imageUrl: contact['image']!,
                    fit: BoxFit.cover,
                    placeholder: (context, url) => Container(
                      color: Colors.grey[200],
                      child: Icon(Icons.person, color: Colors.grey[400]),
                    ),
                    errorWidget: (context, url, error) => Icon(Icons.person),
                  ),
                ),
              ),
              SizedBox(width: 15),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      contact['name']!,
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: Colors.grey[800],
                      ),
                    ),
                    SizedBox(height: 4),
                    Text(
                      contact['relation']!,
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: Icon(Icons.call, color: Colors.green),
                onPressed: () => _callNumber(contact['phone']!),
                tooltip: 'Call ${contact['name']}',
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getRelationColor(String relation) {
    switch (relation.toLowerCase()) {
      case 'father':
        return Colors.blue;
      case 'mother':
        return Colors.pink;
      case 'school':
        return Colors.orange;
      case 'emergency':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  Future<void> _callNumber(String phoneNumber) async {
    final number = phoneNumber.replaceAll(RegExp(r'[^0-9+]'), '');

    try {
      bool? res = await FlutterPhoneDirectCaller.callNumber(number);
      if (res != true) {
        final url = 'tel:$number';
        if (await canLaunch(url)) {
          await launch(url);
        }
      }
    } catch (e) {
      print('Error calling $number: $e');
      final url = 'tel:$number';
      if (await canLaunch(url)) {
        await launch(url);
      }
    }
  }

  void _showContactDetails(BuildContext context, Map<String, dynamic> contact) {
    showModalBottomSheet(
      context: context,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(25)),
      ),
      isScrollControlled: true,
      builder: (context) {
        return Container(
          padding: EdgeInsets.all(20),
          height: MediaQuery.of(context).size.height * 0.6,
          child: Column(
            children: [
              Container(
                width: 40,
                height: 5,
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(5),
                ),
              ),
              SizedBox(height: 20),
              Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: _getRelationColor(contact['relation']!),
                    width: 3,
                  ),
                ),
                child: ClipOval(
                  child: CachedNetworkImage(
                    imageUrl: contact['image']!,
                    fit: BoxFit.cover,
                    placeholder: (context, url) => Container(
                      color: Colors.grey[200],
                      child:
                          Icon(Icons.person, size: 50, color: Colors.grey[400]),
                    ),
                    errorWidget: (context, url, error) =>
                        Icon(Icons.person, size: 50),
                  ),
                ),
              ),
              SizedBox(height: 20),
              Text(
                contact['name']!,
                style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  color: Colors.grey[800],
                ),
              ),
              SizedBox(height: 5),
              Text(
                contact['relation']!,
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.grey[600],
                ),
              ),
              SizedBox(height: 30),
              _buildDetailRow(Icons.phone, contact['phone']!, Colors.green),
              SizedBox(height: 15),
              if (contact['email']!.isNotEmpty)
                _buildDetailRow(Icons.email, contact['email']!, Colors.blue),
              SizedBox(height: 15),
              if (contact['address']!.isNotEmpty)
                _buildDetailRow(
                    Icons.location_on, contact['address']!, Colors.red),
              SizedBox(height: 30),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  ElevatedButton.icon(
                    onPressed: () => _callNumber(contact['phone']!),
                    icon: Icon(Icons.call, size: 20),
                    label: Text('Call'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                      padding:
                          EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                    ),
                  ),
                  if (contact['email']!.isNotEmpty)
                    ElevatedButton.icon(
                      onPressed: () async {
                        final url = 'mailto:${contact['email']}';
                        if (await canLaunch(url)) {
                          await launch(url);
                        }
                      },
                      icon: Icon(Icons.email, size: 20),
                      label: Text('Email'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                        padding:
                            EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      ),
                    ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildDetailRow(IconData icon, String text, Color color) {
    return Row(
      children: [
        Icon(icon, color: color, size: 24),
        SizedBox(width: 15),
        Expanded(
          child: Text(
            text,
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[700],
            ),
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text(
          'Emergency Contacts',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        centerTitle: true,
        backgroundColor: Color(0xFF5C6BC0),
        elevation: 0,
        actions: [
          IconButton(
            icon: Icon(Icons.search, color: Colors.white),
            onPressed: () {
              // Implement search functionality
            },
          ),
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : Column(
              children: [
                Container(
                  padding: EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: Color(0xFF5C6BC0),
                    borderRadius: BorderRadius.only(
                      bottomLeft: Radius.circular(30),
                      bottomRight: Radius.circular(30),
                    ),
                  ),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Icon(Icons.warning_amber,
                              color: Colors.amber, size: 30),
                          SizedBox(width: 10),
                          Text(
                            'Emergency Contacts',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ],
                      ),
                      SizedBox(height: 10),
                      Text(
                        'Quick access to important contacts in case of emergencies',
                        style: TextStyle(
                          color: Colors.white.withOpacity(0.9),
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 20, vertical: 15),
                  child: Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            'Quick Actions',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.grey[800],
                            ),
                          ),
                          IconButton(
                            icon: Icon(Icons.edit, size: 20),
                            onPressed: () => _showEditEmergencyNumbersDialog(),
                            tooltip: 'Edit emergency numbers',
                          ),
                        ],
                      ),
                      SizedBox(height: 10),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          _buildQuickActionButton(
                            icon: Icons.local_hospital,
                            label: 'Ambulance',
                            color: Colors.red,
                            onPressed: () =>
                                _callNumber(emergencyNumbers['Ambulance']!),
                          ),
                          _buildQuickActionButton(
                            icon: Icons.local_police,
                            label: 'Police',
                            color: Colors.blueGrey,
                            onPressed: () =>
                                _callNumber(emergencyNumbers['Police']!),
                          ),
                          _buildQuickActionButton(
                            icon: Icons.fire_truck,
                            label: 'Fire',
                            color: Colors.orange,
                            onPressed: () =>
                                _callNumber(emergencyNumbers['Fire']!),
                          ),
                          _buildQuickActionButton(
                            icon: Icons.call,
                            label: 'Emergency',
                            color: Colors.purple,
                            onPressed: () =>
                                _callNumber(emergencyNumbers['Emergency']!),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 20, vertical: 5),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'My Contacts',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.grey[800],
                        ),
                      ),
                      Text(
                        '${contacts.length} contacts',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[600],
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: ListView.builder(
                    padding: EdgeInsets.only(bottom: 20),
                    itemCount: contacts.length,
                    itemBuilder: (context, index) {
                      final contact = contacts[index];
                      return _buildContactCard(context, contact);
                    },
                  ),
                ),
              ],
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // Add new contact functionality
        },
        child: Icon(Icons.person_add, color: Colors.white),
        backgroundColor: Color(0xFF5C6BC0),
        elevation: 4,
      ),
    );
  }
}
