import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:Safe_pulse/Completeprofile.dart';
import 'package:Safe_pulse/Pages/LinkUserPage.dart';
import 'package:Safe_pulse/Pages/ProfilePage%20.dart';

import 'package:Safe_pulse/Signup/LoginPage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ProfileManagementPage extends StatefulWidget {
  @override
  State<ProfileManagementPage> createState() => _ProfileManagementPageState();
}

class _ProfileManagementPageState extends State<ProfileManagementPage> {
  String? userEmail;
  String? userName;
  Map<String, dynamic>? _userProfile;
  bool _isLoading = true;
  String? _errorMessage;
  final ApiService _apiService = ApiService(); // Assume you have this service

  @override
  void initState() {
    super.initState();
    _loadUserData();
    _loadProfile();
  }

  Future<void> _loadUserData() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    setState(() {
      userEmail = prefs.getString("username") ?? "No email found";
      userName = prefs.getString("name") ?? "User";
    });
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
    }
  }

  Future<void> _logout() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => LoginPage()),
    );
  }

  Widget _buildProfileHeader() {
    return Container(
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
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                color: Colors.white,
                width: 3,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 10,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: ClipOval(
              child: _userProfile!['profileImg'] != null
                  ? CachedNetworkImage(
                      imageUrl:
                          '${ApiService.baseUrl}/${_userProfile!['profileImg']}',
                      fit: BoxFit.cover,
                      placeholder: (context, url) => Container(
                        color: Colors.grey[200],
                        child: Icon(Icons.person,
                            size: 50, color: Colors.grey[400]),
                      ),
                      errorWidget: (context, url, error) =>
                          Icon(Icons.person, size: 50),
                    )
                  : Image.asset(
                      'assets/default_profile.png',
                      fit: BoxFit.cover,
                    ),
            ),
          ),
          SizedBox(height: 16),
          Text(
            _userProfile != null
                ? "${_userProfile!['firstName']} ${_userProfile!['lastName']}"
                : userName ?? "User",
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          SizedBox(height: 8),
          Text(
            _userProfile?['email'] ?? userEmail ?? "Loading...",
            style: TextStyle(
              fontSize: 14,
              color: Colors.white.withOpacity(0.9),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileButton() {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: ElevatedButton.icon(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => EditProfilePage(
                userProfile: _userProfile,
                onProfileUpdated: _loadProfile,
              ),
            ),
          );
        },
        icon: Icon(Icons.edit, color: Colors.white),
        label: Text(
          "Edit Profile",
          style: TextStyle(color: Colors.white, fontSize: 16),
        ),
        style: ElevatedButton.styleFrom(
          backgroundColor: Color(0xFF5C6BC0),
          padding: EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          elevation: 3,
        ),
      ),
    );
  }

  Widget _buildSectionCard(String title, List<Widget> children) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(15),
      ),
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.only(bottom: 12),
              child: Text(
                title,
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF5C6BC0),
                ),
              ),
            ),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildSettingOption(IconData icon, String label, VoidCallback onTap) {
    return ListTile(
      contentPadding: EdgeInsets.symmetric(vertical: 4),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: Color(0xFF5C6BC0).withOpacity(0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(icon, color: Color(0xFF5C6BC0)),
      ),
      title: Text(
        label,
        style: TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
      ),
      trailing: Icon(Icons.arrow_forward_ios, size: 18, color: Colors.grey),
      onTap: onTap,
    );
  }

  Widget _buildProfileInfoItem(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Text(
            '$label: ',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Colors.grey[700],
            ),
          ),
          Text(
            value ?? 'Not provided',
            style: TextStyle(
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogoutButton() {
    return Container(
      margin: EdgeInsets.all(16),
      child: ElevatedButton(
        onPressed: _logout,
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.red[400],
          padding: EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          elevation: 3,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.logout, color: Colors.white),
            SizedBox(width: 10),
            Text(
              "Logout",
              style: TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(
          title: Text(
            'Profile Management',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          centerTitle: true,
          backgroundColor: Color(0xFF5C6BC0),
          elevation: 0,
        ),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_errorMessage != null) {
      return Scaffold(
        appBar: AppBar(
          title: Text(
            'Profile Management',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          centerTitle: true,
          backgroundColor: Color(0xFF5C6BC0),
          elevation: 0,
        ),
        body: Center(child: Text('Error: $_errorMessage')),
      );
    }

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text(
          'Profile Management',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        centerTitle: true,
        backgroundColor: Color(0xFF5C6BC0),
        elevation: 0,
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: ListView(
                physics: ClampingScrollPhysics(),
                children: [
                  _buildProfileHeader(),
                  _buildProfileButton(),
                  _buildSectionCard(
                    "Personal Information",
                    [
                      _buildProfileInfoItem(
                          "First Name", _userProfile?['firstName']),
                      _buildProfileInfoItem(
                          "Last Name", _userProfile?['lastName']),
                      _buildProfileInfoItem("Gender", _userProfile?['gender']),
                      _buildProfileInfoItem("Mobile", _userProfile?['mobile']),
                      _buildProfileInfoItem(
                          "Date of Birth", _userProfile?['dateOfBirth']),
                      _buildProfileInfoItem(
                          "Age", _userProfile?['age']?.toString()),
                    ],
                  ),
                  _buildSectionCard(
                    "Account Settings",
                    [
                      _buildSettingOption(Icons.lock, "Change Password", () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => ChangePasswordPage()),
                        );
                      }),
                      _buildSettingOption(Icons.phone, "Update Phone Number",
                          () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => UpdatePhonePage()),
                        );
                      }),
                    ],
                  ),
                  _buildSectionCard(
                    "Privacy & Security",
                    [
                      _buildSettingOption(Icons.link, "Link User Account",
                          () async {
                        SharedPreferences prefs =
                            await SharedPreferences.getInstance();
                        String? email = prefs.getString("username");
                        if (email != null) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) =>
                                  LinkUserPage(userEmail: email),
                            ),
                          );
                        }
                      }),
                      _buildSettingOption(Icons.privacy_tip, "Privacy Policy",
                          () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => PrivacyPolicyPage()),
                        );
                      }),
                    ],
                  ),
                  _buildLogoutButton(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class ChangePasswordPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Change Password")),
      body: Center(child: Text("Change Password Page")),
    );
  }
}

class UpdatePhonePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Update Phone Number")),
      body: Center(child: Text("Update Phone Number Page")),
    );
  }
}

class PrivacyPolicyPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Privacy Policy")),
      body: Center(child: Text("Privacy Policy Page")),
    );
  }
}
