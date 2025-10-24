package com.maarco.controler;

import com.maarco.entities.User;
import com.maarco.event.RegistrationCompleteEvent;
import com.maarco.registration.RegistrationRequest;
import com.maarco.registration.token.VerificationToken;
import com.maarco.registration.token.VerificationTokenRepository;
import com.maarco.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
//@RequestMapping("/api/v1/auth/")
@Tag(name = "Registration Controller", description = "This is Registration Controller")
public class RegistrationController {

    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;

    //for registering the user n application
    @PostMapping
    public String registerUser(@RequestBody RegistrationRequest registrationRequest, final HttpServletRequest request) {
        User user = userService.registerUser(registrationRequest);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        return "Success!  Please, check your email for to complete your registration";
    }

    // to verify the e mail
    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        VerificationToken theToken = tokenRepository.findByToken(token);
        if (theToken.getUser().isEnabled()) {
            return "This account has already been verified, please, login.";
        }
        String verificationResult = userService.validateToken(token);
        if (verificationResult.equalsIgnoreCase("valid")) {
            return "Email verified successfully. Now you can login to your account";
        }
        return "Invalid verification token";
    }

    // application uniform resource locator
    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
