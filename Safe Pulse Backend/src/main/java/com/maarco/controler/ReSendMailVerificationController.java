package com.maarco.controler;

import com.maarco.entities.User;
import com.maarco.registration.token.VerificationTokenRepository;
import com.maarco.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register/resendVerificationEmail")
public class ReSendMailVerificationController {

    @Autowired
    private UserService userService;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private JavaMailSender mailSender;

    //asdasdasd

    // to send the verification mail again
    @GetMapping("/resendVerification")
    public String resendVerificationEmail(@RequestParam("email") String email, HttpServletRequest request) {
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User theUser = userOptional.get();

            // Check if the user's email is already verified
            if (theUser.isEmailVerified()) {
                return "Email has already been verified. Please log in.";
            }

            // Step 2: Generate a new verification token
            String newVerificationToken;
            do {
                newVerificationToken = generateNewVerificationToken();
            } while (tokenRepository.findByToken(newVerificationToken) != null);

            // Step 3: Update the user's token in the database
            userService.saveUserVerificationToken(theUser, newVerificationToken);

            // Step 4: Get the base URL of the application
            String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

            // Step 5: Send the new verification email with the new token using your EmailService
            sendVerificationEmail(theUser, newVerificationToken, appUrl);

            return "A new verification email has been sent. Please check your email.";
        } else {
            return "User not found. Please check the email address.";
        }
    }

    // Helper method to generate a new verification token
    private String generateNewVerificationToken() {
        return UUID.randomUUID().toString();
    }

    // to send the verification mail to the user
    public void sendVerificationEmail(User theUser, String verificationToken, String applicationUrl) {
        String subject = "Email Verification";
        String senderName = "Safe Pulse";
        String mailContent = "<p> Hi, " + theUser.getEmail() + ", </p>" + "<p>Thank you for registering with us. Please follow the link below to complete your registration:</p>" + "<a href=\"" + applicationUrl + "/register/verifyEmail?token=" + verificationToken + "\">Verify your email to activate your account</a>" + "<p> Thank you <br> Users Registration Portal Service";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message);
            messageHelper.setFrom("o2i.irus.tech@gmail.com", senderName);
            messageHelper.setTo(theUser.getEmail());
            messageHelper.setSubject(subject);
            messageHelper.setText(mailContent, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            // Handle the exceptions here, for example, log the error or throw a custom exception
            throw new RuntimeException("Error sending email", e);
        }
    }



//    public void sendVerificationEmail(User theUser, String verificationToken, String applicationUrl) {
//        String subject = "Email Verification";
//        String senderName = "Nutrify India Now (2.O)";
//        String verificationLink = applicationUrl + "/register/verifyEmail?token=" + verificationToken;
//        String mailContent = "<p> Hi, " + theUser.getEmail() + ", </p>" + "<p>Thank you for registering with us. Please follow the link below to complete your registration:</p>" + "<a href=\"" + verificationLink + "\">Verify your email to activate your account</a>" + "<p> Thank you <br> Users Registration Portal Service";
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            var messageHelper = new MimeMessageHelper(message);
//            messageHelper.setFrom("rajkumariimt2002@gmail.com", senderName);
//            messageHelper.setTo(theUser.getEmail());
//            messageHelper.setSubject(subject);
//            messageHelper.setText(mailContent, true);
//            mailSender.send(message);
//
//            // Print the verification link to the console
//            System.out.println("Verification Link: " + verificationLink);
//        } catch (MessagingException | UnsupportedEncodingException e) {
//            // Handle the exceptions here, for example, log the error or throw a custom exception
//            throw new RuntimeException("Error sending email", e);
//        }
//    }


}
