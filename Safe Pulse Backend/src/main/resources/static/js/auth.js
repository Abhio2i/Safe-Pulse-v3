//
//     // Example usage in your API request function
//     function makeApiRequest() {
//         // Get the JWT token from cookies
//         const jwtToken = getCookie('jwtToken');
//         console.log('JWT Token:', jwtToken);
//
//         // Make the API request with the token in the Auth header
//         fetch('/dashboard/dashboard.html', {
//             method: 'GET',
//             headers: {
//                 'Auth': 'Bearer ' + jwtToken,
//                 'Content-Type': 'application/json',
//             },
//         })
//             .then(response => response.json())
//             .then(data => {
//                 // Handle the data returned from the API
//                 console.log('API Data:', data);
//             })
//             .catch(error => {
//                 console.error('Error during API request:', error);
//                 alert('An error occurred during API request. Please try again later.');
//             });
//     }
//
//     // Example function to get the value of a cookie by name
//     function getCookie(name) {
//         const cookieName = `${name}=`;
//         const decodedCookies = decodeURIComponent(document.cookie);
//         const cookieArray = decodedCookies.split(';');
//
//         for (let i = 0; i < cookieArray.length; i++) {
//             let cookie = cookieArray[i].trim();
//
//             if (cookie.indexOf(cookieName) === 0) {
//                 return cookie.substring(cookieName.length, cookie.length);
//             }
//         }
//
//         return null;
//     }
//
//
//     // Function to set a cookie with a given name and value
//     function setCookie(name, value) {
//         document.cookie = `${name}=${value}; path=/`;
//     }
//
//
//     // Function to get the value of a cookie by name
// //     function getCookie(name) {
// //     const cookieName = `${name}=`;
// //     const decodedCookies = decodeURIComponent(document.cookie);
// //     const cookieArray = decodedCookies.split(';');
// //
// //     for (let i = 0; i < cookieArray.length; i++) {
// //     let cookie = cookieArray[i].trim();
// //
// //     if (cookie.indexOf(cookieName) === 0) {
// //     return cookie.substring(cookieName.length, cookie.length);
// // }
// // }
// //
// //     return null;
// // }
//
//
//     function signIn() {
//         // Reset previous error messages
//         document.getElementById("emailError").textContent = "";
//         document.getElementById("passwordError").textContent = "";
//
//         // Get user input (email and password) from the form
//         const email = document.getElementById("emailInput").value;
//         const password = document.getElementById("passwordInput").value;
//
//         // Make the API request to your backend
//         fetch('/dashboard/user-login', {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json',
//             },
//             body: JSON.stringify({
//                 email: email,
//                 password: password,
//             }),
//         })
//             .then(response => {
//                 if (!response.ok) {
//                     throw new Error(`HTTP error! Status: ${response.status}`);
//                 }
//
//                 // Access the JWT token from the response headers
//                 const jwtToken = response.headers.get('Auth').split(' ')[1];
//
//                 console.log('JWT Token:', jwtToken);
//
//                 // Save the token in cookies
//                 setCookie('jwtToken', jwtToken);
//
//                 // Call the makeApiRequest function after successful login
//                 makeApiRequest();
//             })
//             .catch(error => {
//                 console.error('Error during API request:', error);
//
//                 if (error.message.includes('Incorrect email or password')) {
//                     // Handle incorrect email or password error
//                     document.getElementById("passwordError").textContent = 'Incorrect email or password. Please try again.';
//                 } else {
//                     // Handle other errors (display a generic error message, redirect, etc.)
//                     console.error('An error occurred during API request. Please try again later.');
//                     alert('An error occurred during API request. Please try again later.');
//                 }
//             })
//             .finally(() => {
//                 // Redirect to the dashboard.html page regardless of the error
//                 window.location.href = './dashboard.html';
//             });
//     }


























// Example usage in your API request function
function makeApiRequest() {
    // Get the JWT token from cookies
    const jwtToken = getCookie('jwtToken');
    console.log('JWT Token:', jwtToken);

    // Make the API request with the token in the Auth header
    fetch('/dashboard/dashboard.html', {
        method: 'GET',
        headers: {
            'Auth': 'Bearer ' + jwtToken,
            'Content-Type': 'application/json', // Include other necessary headers if needed
            // 'Accept': 'application/json',
        },
    })
        .then(response => {
            // Handle the response
            console.log('API Response:', response);

            // Check if the response is successful (status code 2xx)
            if (response.ok) {
                // Redirect to the dashboard.html page with the JWT token as a query parameter
                window.location.href = `./dashboard.html`;
            } else {
                // Handle non-successful response (optional)
                console.error('API request failed with status:', response.status);
                alert('API request failed. Please try again.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred during API request. Please try again later.');
        });
}



// Function to set a cookie with a given name and value
function setCookie(name, value) {
    document.cookie = `${name}=${value}; path=/`;
}


// Function to get the value of a cookie by name
function getCookie(name) {
    const cookieName = `${name}=`;
    const decodedCookies = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookies.split(';');

    for (let i = 0; i < cookieArray.length; i++) {
        let cookie = cookieArray[i].trim();

        if (cookie.indexOf(cookieName) === 0) {
            return cookie.substring(cookieName.length, cookie.length);
        }
    }

    return null;
}


function signIn() {
    // Reset previous error messages
    document.getElementById("emailError").textContent = "";
    document.getElementById("passwordError").textContent = "";

    // Get user input (email and password) from the form
    const email = document.getElementById("emailInput").value;
    const password = document.getElementById("passwordInput").value;

    // Make the API request to your backend
    fetch('/dashboard/user-login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: email,
            password: password,
        }),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Access the JWT token from the response headers
            const jwtToken = response.headers.get('Auth').split(' ')[1];

            console.log('JWT Token:', jwtToken);

            // Save the token in cookies
            setCookie('jwtToken', jwtToken);

            // // Redirect to the dashboard with the JWT token
            window.location.href = `./dashboard.html?token=${jwtToken}`;
            // Make the API request and redirect after a successful login
            // makeApiRequest();
        })
        .catch(error => {
            console.error('Error:', error);

            if (error.message.includes('Incorrect email or password')) {
                // Handle incorrect email or password error
                document.getElementById("passwordError").textContent = 'Incorrect email or password. Please try again.';
            } else {
                // Handle other errors (display a generic error message, redirect, etc.)
                alert('An error occurred. Please try again later.');
            }
        });
}