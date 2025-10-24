// import 'package:flutter/material.dart';
// import 'package:google_nav_bar/google_nav_bar.dart';
// import 'package:Safe_pulse/Pages/ParentDashboard.dart';
// import 'package:Safe_pulse/Pages/LiveTrackingPage.dart';
// import 'package:Safe_pulse/Pages/GeofenceSetupPage.dart';
// import 'package:Safe_pulse/Pages/ChatPage.dart';
// import 'package:Safe_pulse/Pages/ProfileManagementPage.dart';
// import 'package:typicons_flutter/typicons_flutter.dart';

// void main() => runApp(MyApp());

// class MyApp extends StatelessWidget {
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       debugShowCheckedModeBanner: false,
//       home: BottomNav(),
//     );
//   }
// }

// class BottomNav extends StatefulWidget {
//   @override
//   _BottomNavState createState() => _BottomNavState();
// }

// class _BottomNavState extends State<BottomNav> {
//   int _selectedIndex = 0;

//   void _onItemTapped(int index) {
//     setState(() {
//       _selectedIndex = index;
//     });
//   }

//   final List<Widget> _pages = [
//     ParentDashboard(),
//     LiveTrackingPage(),
//     GeofenceSetupPage(),
//     ChatPage(),
//     ProfileManagementPage(),
//   ];

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       body: _pages[_selectedIndex],
//       bottomNavigationBar: Container(
//         decoration: BoxDecoration(
//           color: Colors.white,
//           borderRadius: BorderRadius.only(
//             topLeft: Radius.circular(20),
//             topRight: Radius.circular(20),
//           ),
//           boxShadow: [
//             BoxShadow(
//               blurRadius: 20,
//               color: Colors.black.withOpacity(0.2),
//             ),
//           ],
//         ),
//         child: SafeArea(
//           child: Padding(
//             padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8),
//             child: GNav(
//               curve: Curves.easeOutExpo,
//               rippleColor: Colors.grey.shade300,
//               hoverColor: Colors.grey.shade100,
//               haptic: true,
//               tabBorderRadius: 20,
//               gap: 5,
//               activeColor: Colors.white,
//               padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
//               duration: const Duration(milliseconds: 200),
//               tabBackgroundColor: Colors.blue.withOpacity(0.7),
//               textStyle: TextStyle(color: Colors.white, fontSize: 13),
//               tabs: const [
//                 GButton(
//                   iconSize: 28,
//                   icon: Icons.dashboard,
//                   text: 'Dashboard',
//                 ),
//                 GButton(
//                   iconSize: 28,
//                   icon: Icons.location_on,
//                   text: 'Tracking',
//                 ),
//                 GButton(
//                   iconSize: 28,
//                   icon: Icons.map,
//                   text: 'Geofence',
//                 ),
//                 GButton(
//                   iconSize: 28,
//                   icon: Icons.chat,
//                   text: 'Chat',
//                 ),
//                 GButton(
//                   iconSize: 28,
//                   icon: Typicons.user,
//                   text: 'Profile',
//                 ),
//               ],
//               selectedIndex: _selectedIndex,
//               onTabChange: _onItemTapped,
//             ),
//           ),
//         ),
//       ),
//     );
//   }
// }

import 'package:flutter/material.dart';
import 'package:google_nav_bar/google_nav_bar.dart';
import 'package:Safe_pulse/Pages/ParentDashboard.dart';
import 'package:Safe_pulse/Pages/LiveTrackingPage.dart';
import 'package:Safe_pulse/Pages/GeofenceSetupPage.dart';
import 'package:Safe_pulse/Pages/UserRelationsPage.dart';
import 'package:Safe_pulse/Pages/UserRelationsPageRaj.dart';
import 'package:Safe_pulse/Pages/ChatPage.dart';
import 'package:Safe_pulse/Pages/ProfileManagementPage.dart';
import 'package:Safe_pulse/other%20pages/ContactPage.dart';
import 'package:Safe_pulse/other%20pages/ZoneCreationPage.dart';
import 'package:typicons_flutter/typicons_flutter.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: BottomNav(),
    );
  }
}

class BottomNav extends StatefulWidget {
  @override
  _BottomNavState createState() => _BottomNavState();
}

class _BottomNavState extends State<BottomNav> {
  int _selectedIndex = 0;

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  // Updated pages list - replace LiveTrackingPage with UserRelationsPageRaj
  final List<Widget> _pages = [
    UserRelationsPage(),
    ContactPage(), // Replaced LiveTrackingPage with Raj's test page
    ZoneCreationPage(),
    // ChatPage(),
    ProfileManagementPage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_selectedIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(20),
            topRight: Radius.circular(20),
          ),
          boxShadow: [
            BoxShadow(
              blurRadius: 20,
              color: Colors.black.withOpacity(0.2),
            ),
          ],
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8),
            child: GNav(
              curve: Curves.easeOutExpo,
              rippleColor: Colors.grey.shade300,
              hoverColor: Colors.grey.shade100,
              haptic: true,
              tabBorderRadius: 20,
              gap: 5,
              activeColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
              duration: const Duration(milliseconds: 200),
              tabBackgroundColor: Color(0xFF5C6BC0),
              textStyle: TextStyle(color: Colors.white, fontSize: 13),
              tabs: const [
                GButton(
                  iconSize: 28,
                  icon: Icons.dashboard,
                  text: 'Dashboard',
                ),
                GButton(
                  iconSize: 28,
                  icon: Icons.message,
                  text: 'Contact', // This will now open UserRelationsPageRaj
                ),

                GButton(
                  iconSize: 28,
                  icon: Icons.map,
                  text: 'Geofence',
                ),

                // GButton(
                //   iconSize: 28,
                //   icon: Icons.chat,
                //   text: 'Chat',
                // ),
                GButton(
                  iconSize: 28,
                  icon: Typicons.user,
                  text: 'Profile',
                ),
              ],
              selectedIndex: _selectedIndex,
              onTabChange: _onItemTapped,
            ),
          ),
        ),
      ),
    );
  }
}
