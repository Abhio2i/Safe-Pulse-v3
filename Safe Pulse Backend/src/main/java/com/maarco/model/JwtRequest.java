/**
 * Represents an authentication request containing user credentials for JWT generation.
 * Used as the request body for login/authentication endpoints.
 */
package com.maarco.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JwtRequest {

    /**
     * User's email address serving as username
     * Should be validated as proper email format
     */
    private String email;

    /**
     * User's raw password (will be encrypted/hashed during processing)
     * Should never be stored or logged in plain text
     */
    private String password;
}