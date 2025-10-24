package com.maarco.controler;

import com.maarco.entities.Role;
import com.maarco.entities.User;

import com.maarco.exception.UserNotFoundException;
import com.maarco.model.JwtRequest;
import com.maarco.model.JwtResponse;
import com.maarco.repository.UserRepository;
import com.maarco.request.ChangePasswordRequest;
import com.maarco.request.RefreshTokenRequest;
import com.maarco.security.JwtHelper;
import com.maarco.security.Refresh.RefreshToken;
import com.maarco.security.Refresh.RefreshTokenService;
import com.maarco.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "Api for Authentication")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtHelper helper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRepository userDao;
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private RefreshTokenService refreshTokenService;


    private Logger logger = LoggerFactory.getLogger(AuthController.class);

// for user's login
//    @PostMapping("/login")
//    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request)
//    {
//        // Authenticate user
//        this.doAuthenticate(request.getEmail(), request.getPassword());
//
//
//        // Load user details
//        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
//        // Generate JWT token
//        String token = this.helper.generateToken(userDetails);
//
//        // Generate refresh token
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
//
//        Optional<User> user = userDao.findByEmail(request.getEmail());
//
//      User usr = user.get();
//
//
//        JwtResponse response = JwtResponse.builder()
//                .jwtToken(token)
//                .refreshToken(refreshToken.getRefreshToken())
//                .userId(usr.getUserId().toString())
//                .username(userDetails.getUsername()).build();
//        return new ResponseEntity<>(response, HttpStatus.OK);
//
//    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {
        // Authenticate user
        this.doAuthenticate(request.getEmail(), request.getPassword());

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Generate JWT token
        String token = this.helper.generateToken(userDetails);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // Get user details from database
        Optional<User> userOptional = userDao.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User usr = userOptional.get();

        // Extract a single role (assuming user has at least one role)
        String role = usr.getRoles().stream()
                .findFirst() // Get the first role
                .map(Role::getName) // Extract role name
                .orElse("ROLE_USER");

        // Build response object
        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .refreshToken(refreshToken.getRefreshToken())
                .userId(usr.getUserId())
                .username(userDetails.getUsername())
                .role(role) // Add single role to response
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // do authentication of the user
    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
                manager.authenticate(authentication);
        }catch (BadCredentialsException e){
                throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }


// Exception handler for BadCredentialsException
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials Invalid !!");
}

    // to change the password of the specific user
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        // Extract the token from the Authorization header (assuming it's in the format "Bearer <token>")
        String tokenHeader = request.getHeader("Auth");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.replace("Bearer ", "");

            // Extract the username (email) from the token
            String username = jwtHelper.getUsernameFromToken(token);

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found for email: " + username));
            // Check if the old password matches the user's current password
            if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
                // Old password matches, proceed with the password change.
                user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                userRepository.save(user);
                return new ResponseEntity<>("Password changed successfully.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Old password is incorrect.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Unauthorized. Please provide a valid JWT token.", HttpStatus.UNAUTHORIZED);
        }
    }



    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshJwtToken(@RequestBody RefreshTokenRequest request){

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail()); // Assuming email is the username

        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .refreshToken(refreshToken.getRefreshToken())
                .userId(user.getUserId().toString())
                .username(userDetails.getUsername())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
