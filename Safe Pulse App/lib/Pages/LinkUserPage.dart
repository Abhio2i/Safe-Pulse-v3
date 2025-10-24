import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:Safe_pulse/Api/ApiService.dart';
import 'package:Safe_pulse/Pages/ViewRequestsPage.dart';

class LinkUserPage extends StatefulWidget {
  final String userEmail;

  LinkUserPage({required this.userEmail});

  @override
  _LinkUserPageState createState() => _LinkUserPageState();
}

class _LinkUserPageState extends State<LinkUserPage> {
  final TextEditingController _toEmailController = TextEditingController();
  final TextEditingController _relationNameController = TextEditingController();
  bool _isLoading = false;
  String _message = "";

  Future<void> _sendRelationshipRequest() async {
    setState(() {
      _isLoading = true;
      _message = "";
    });

    final String toEmail = _toEmailController.text.trim();
    final String relationName = _relationNameController.text.trim();

    if (toEmail.isEmpty || relationName.isEmpty) {
      setState(() {
        _message = "Please fill all fields.";
        _isLoading = false;
      });
      return;
    }

    final String url =
        '${ApiService.baseUrl}/api/relationships/sendRequest?fromEmail=${widget.userEmail}&toEmail=$toEmail&relationName=$relationName';

    try {
      final response = await http.post(Uri.parse(url));

      if (response.statusCode == 200) {
        setState(() {
          _message =
              "Relationship request sent successfully!\nPlease ask the other user to accept the request.";
        });
      } else {
        setState(() {
          _message = "Failed to send request. Please try again.";
        });
      }
    } catch (e) {
      setState(() {
        _message = "An error occurred. Please check your connection.";
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: Text("Link User"),
        backgroundColor: Colors.blue[800],
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              "Send Relationship Request",
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.blue[800],
              ),
            ),
            SizedBox(height: 20),
            TextField(
              controller: _toEmailController,
              decoration: InputDecoration(
                labelText: "Recipient Email",
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 10),
            TextField(
              controller: _relationNameController,
              decoration: InputDecoration(
                labelText: "Relationship (e.g., Brother, Sister)",
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 20),
            _isLoading
                ? Center(child: CircularProgressIndicator())
                : ElevatedButton(
                    onPressed: _sendRelationshipRequest,
                    child: Text("Send Request"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue[800],
                      padding: EdgeInsets.symmetric(vertical: 15),
                      minimumSize: Size(double.infinity, 50),
                    ),
                  ),
            SizedBox(height: 20),
            if (_message.isNotEmpty)
              Text(
                _message,
                style: TextStyle(
                  color: _message.contains("successfully")
                      ? Colors.green
                      : Colors.red,
                  fontSize: 16,
                ),
              ),
            SizedBox(height: 20),
            TextButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) =>
                        ViewRequestsPage(userEmail: widget.userEmail),
                  ),
                );
              },
              child: Text(
                "View All Requests",
                style: TextStyle(
                  color: Colors.blue[800],
                  fontSize: 16,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
