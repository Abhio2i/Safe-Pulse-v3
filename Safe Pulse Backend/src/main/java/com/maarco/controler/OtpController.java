package com.maarco.controler;



import com.maarco.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public String sendOtp(@RequestParam String phoneNumber) {
        try {
            return otpService.sendOtp(phoneNumber);
        } catch (Exception e) {
            return "Failed to send OTP: " + e.getMessage();
        }
    }
}
