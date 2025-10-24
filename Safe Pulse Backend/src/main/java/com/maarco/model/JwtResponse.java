/**
 * Represents the authentication response containing JWT tokens and user details
 * returned after successful login/authentication.
 */
package com.maarco.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JwtResponse {

    /**
     * Short-lived JWT access token for API authorization
     * Typically expires in 15-30 minutes
     */
    private String jwtToken;

    /**
     * Long-lived refresh token for obtaining new access tokens
     * Typically expires in 7-30 days
     */
    private String refreshToken;

    /**
     * User's display name or username
     */
    private String username;

    /**
     * Unique identifier for the authenticated user
     */
    private String userId;

    /**
     * User's primary role (simplified from list to single role)
     * Examples: "ROLE_USER", "ROLE_ADMIN"
     */
    private String role;
}