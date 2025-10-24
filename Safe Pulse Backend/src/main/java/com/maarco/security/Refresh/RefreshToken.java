/**
 * Represents a refresh token entity used for JWT authentication.
 * Stores the refresh token string, its expiration time, and associates it with a specific user.
 * This entity is persisted in MongoDB and used to generate new access tokens
 * when existing ones expire, maintaining continuous user sessions.
 */


package com.maarco.security.Refresh;
import com.maarco.entities.User;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Collation
public class RefreshToken {
    @Id
    private String tokenId;

    private String refreshToken;

    private Instant expiry;
    @DBRef
    private User user;
}
