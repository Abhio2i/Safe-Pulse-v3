package com.maarco.registration.token;

import com.maarco.entities.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Collation
@NoArgsConstructor
public class VerificationToken {
    private static final int TOKEN_LENGTH = 32; // Length of the token in characters
    private static final int EXPIRATION_TIME = 15;
    @Id
    private String id;
    private String token;
    private Date expirationTime;

    @DBRef
    private User user;

    public VerificationToken(String token, User user) {
        super();
        this.token = token;
        this.user = user;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public static String generateNewVerificationToken() {
        // Generate a random part
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Generate a unique timestamp-based part
        long timestamp = System.currentTimeMillis();
        String timestampPart = Long.toString(timestamp);

        // Combine random and timestamp parts
        String combinedToken = randomPart + timestampPart;

        // Add a UUID to ensure uniqueness
        String uniqueToken = combinedToken + "-" + UUID.randomUUID();

        return uniqueToken;
    }

    public Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);
        return new Date(calendar.getTime().getTime());
    }
}
