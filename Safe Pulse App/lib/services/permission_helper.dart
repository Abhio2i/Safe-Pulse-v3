// import 'package:permission_handler/permission_handler.dart';

// class PermissionHelper {
//   static Future<void> requestPermissions() async {
//     // Request Notification Permission first
//     PermissionStatus notificationStatus =
//         await Permission.notification.request();

//     if (notificationStatus.isDenied) {
//       print("Notification permission denied.");
//       return;
//     }

//     // Request Location Permission
//     PermissionStatus locationStatus = await Permission.location.request();

//     if (locationStatus.isDenied) {
//       print("Location permission denied.");
//       return;
//     }

//     // Request Location Always Permission
//     PermissionStatus locationAlwaysStatus =
//         await Permission.locationAlways.request();

//     if (!locationAlwaysStatus.isGranted) {
//       print("Location Always permission denied.");
//       return;
//     }

//     print("All required permissions granted.");
//   }
// }



// import 'package:flutter/material.dart';
// import 'package:permission_handler/permission_handler.dart';

// class PermissionHelper {
//   static Future<void> requestPermissions(BuildContext context) async {
//     // Request Notification Permission first
//     PermissionStatus notificationStatus =
//         await Permission.notification.request();

//     if (notificationStatus.isDenied) {
//       print("Notification permission denied.");
//       return;
//     }

//     // Show dialog with location data usage message
//     bool? proceed = await _showLocationPermissionDialog(context);
//     if (proceed != true) {
//       print("User canceled location permission request.");
//       return;
//     }

//     // Request Location Permission
//     PermissionStatus locationStatus = await Permission.location.request();

//     if (locationStatus.isDenied) {
//       print("Location permission denied.");
//       return;
//     }

//     // Request Location Always Permission
//     PermissionStatus locationAlwaysStatus =
//         await Permission.locationAlways.request();

//     if (!locationAlwaysStatus.isGranted) {
//       print("Location Always permission denied.");
//       return;
//     }

//     print("All required permissions granted.");
//   }

//   static Future<bool?> _showLocationPermissionDialog(BuildContext context) async {
//     return showDialog<bool>(
//       context: context,
//       barrierDismissible: false, // Prevents dismissing by tapping outside
//       builder: (BuildContext context) {
//         return AlertDialog(
//           title: const Text('Location Permission Required'),
//           content: const Text(
//             'Safe Pulse collects your location data to enable real-time location tracking and emergency safety alerts, even when the app is closed or not in use.',
//           ),
//           actions: <Widget>[
//             TextButton(
//               child: const Text('Cancel'),
//               onPressed: () {
//                 Navigator.of(context).pop(false); // User cancels
//               },
//             ),
//             TextButton(
//               child: const Text('Allow'),
//               onPressed: () {
//                 Navigator.of(context).pop(true); // User agrees
//               },
//             ),
//           ],
//         );
//     },
//     );
//   }
// }



import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

class PermissionHelper {
  static Future<void> requestPermissions(BuildContext context) async {
    // Request Notification Permission first
    PermissionStatus notificationStatus =
        await Permission.notification.request();

    if (notificationStatus.isDenied) {
      print("Notification permission denied.");
      return;
    }

    // Show dialog with location data usage message
    bool? proceed = await _showLocationPermissionDialog(context);
    if (proceed != true) {
      print("User canceled location permission request.");
      return;
    }

    // Request Location Permission
    PermissionStatus locationStatus = await Permission.location.request();

    if (locationStatus.isDenied) {
      print("Location permission denied.");
      return;
    }

    // Request Location Always Permission
    PermissionStatus locationAlwaysStatus =
        await Permission.locationAlways.request();

    if (!locationAlwaysStatus.isGranted) {
      print("Location Always permission denied.");
      return;
    }

    print("All required permissions granted.");
  }

  static Future<bool?> _showLocationPermissionDialog(BuildContext context) async {
    return showDialog<bool>(
      context: context,
      barrierDismissible: false, // Prevents dismissing by tapping outside
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Location Permission Required'),
          content: const Text(
            'Safe Pulse collects your location data to enable real-time location tracking and emergency safety alerts, even when the app is closed or not in use.\n\n'
            'Note: By clicking Allow/Accept, you are giving permission to the Safe Pulse app to access location data.',
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Cancel'),
              onPressed: () {
                Navigator.of(context).pop(false); // User cancels
              },
            ),
            TextButton(
              child: const Text('Allow'),
              onPressed: () {
                Navigator.of(context).pop(true); // User agrees
              },
            ),
          ],
        );
      },
    );
  }
}