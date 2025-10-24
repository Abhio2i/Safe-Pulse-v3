package com.maarco.request;

import lombok.Data;

/**
 * Request DTO for refresh token operations.
 * Contains the refresh token needed to obtain a new access token
 * when the current access token has expired.
 */
@Data
public class RefreshTokenRequest {
    /**
     * The refresh token string that was previously issued
     * during authentication
     */
    private String refreshToken;
}