import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart'; // Import the Lottie package
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:Safe_pulse/Signup/Register.dart';
import 'package:Safe_pulse/BottomPages/BottomNav.dart';
import 'package:Safe_pulse/services/background_services.dart';
import 'package:Safe_pulse/services/permission_helper.dart';
// Import your RegisterPage

class LoginPage extends StatefulWidget {
  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  String _email = '';
  String _password = '';
  bool _isLoading = false;

  final ApiService _apiService = ApiService(); // Instance of ApiService

  // Function to handle login
  void _loginUser() async {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save(); // Save the form values
      setState(() {
        _isLoading = true;
      });

      try {
        final response = await _apiService.loginUser(
          email: _email.trim(),
          password: _password.trim(),
        );

        // Show success message
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              backgroundColor: Colors.green,
              content: Text("Login Successful!")),
        );
        await initializeService();
        // Redirect to another page after login
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
              builder: (context) =>
                  BottomNav()), // Replace with your desired page
        );
      } catch (e) {
        // Show error message
        print("Error: hhhh$e");
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Error: $e")),
        );
      } finally {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _requestPermissions() async {
    await PermissionHelper.requestPermissions(context);
  }

  @override
  void initState() {
    super.initState();
    _requestPermissions(); // Call the method here
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        // Prevent back navigation
        return false;
      },
      child: Scaffold(
        backgroundColor: Colors.white,
        appBar: AppBar(
          title: Text("Login"),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Form(
            key: _formKey,
            child: ListView(
              children: [
                // Add Lottie animation at the top
                Center(
                  child: Lottie.asset(
                    'assets/anima.json', // Path to your Lottie file
                    height: 300,
                    width: 300,
                  ),
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: InputDecoration(
                    labelText: "Email",
                    hintText: "Enter your email address",
                    prefixIcon: Icon(Icons.email),
                    filled: true,
                    fillColor: Colors.grey.shade100,
                    contentPadding:
                        EdgeInsets.symmetric(vertical: 15, horizontal: 20),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none, // Removes the border
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    errorBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    focusedErrorBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                  ),
                  keyboardType: TextInputType.emailAddress,
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return "Email cannot be empty";
                    } else if (!RegExp(
                            r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$")
                        .hasMatch(value)) {
                      return "Please provide a valid email address";
                    }
                    return null;
                  },
                  onSaved: (value) {
                    _email = value!;
                  },
                ),
                SizedBox(height: 16),
                TextFormField(
                  decoration: InputDecoration(
                    labelText: "Password",
                    hintText: "Enter your password",
                    prefixIcon: Icon(Icons.lock),
                    filled: true,
                    fillColor: Colors.grey.shade100,
                    contentPadding:
                        EdgeInsets.symmetric(vertical: 15, horizontal: 20),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none, // Removes the border
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    errorBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    focusedErrorBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                  ),
                  obscureText: true,
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return "Please enter your password";
                    }
                    return null;
                  },
                  onSaved: (value) {
                    _password = value!;
                  },
                ),
                SizedBox(height: 16),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: SizedBox(
                    width: MediaQuery.of(context).size.width *
                        0.8, // Responsive width
                    child: ElevatedButton(
                      onPressed: _isLoading
                          ? null
                          : _loginUser, // Disable button while loading
                      child: Padding(
                        padding: const EdgeInsets.all(
                            15.0), // Adjust padding for a better tap area
                        child: _isLoading
                            ? SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : Text(
                                "Login",
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                      ),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color.fromARGB(
                            255, 122, 196, 125), // Match green color
                        shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.circular(16), // Rounded corners
                        ),
                        textStyle: const TextStyle(fontWeight: FontWeight.w600),
                        elevation: 5, // Subtle shadow for a raised effect
                      ),
                    ),
                  ),
                ),

                SizedBox(height: 16),
                // Add "Do not have an account? Register" text
                Center(
                  child: GestureDetector(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) =>
                              RegisterPage(), // Navigate to RegisterPage
                        ),
                      );
                    },
                    child: Text(
                      "Don't have an account? Register",
                      style: TextStyle(
                        color: Colors.red,
                        fontSize: 16,
                        decoration: TextDecoration.underline,
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
}
