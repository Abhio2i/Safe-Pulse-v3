//package com.maarco.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//public class JwtHelper {
////    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;
//public static final long JWT_TOKEN_VALIDITY =  7 * 24 * 60 * 60;;
//
////    public static final long JWT_TOKEN_VALIDITY = 20;
////    public static final long JWT_TOKEN_VALIDITY = Long.MAX_VALUE;
//
//    private final String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
//
//    //retrieve username from jwt token
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    //retrieve expiration date from jwt token
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    //for retrieveing any information from token we will need the secret key
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
//    }
//
//    //check if the token has expired
//    private Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    //generate token for user
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return doGenerateToken(claims, userDetails.getUsername());
//    }
//
//    //for token generation
//    private String doGenerateToken(Map<String, Object> claims, String subject) {
//
//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)).signWith(SignatureAlgorithm.HS512, secret).compact();
//    }
//
//    //validate token
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        final String username = getUsernameFromToken(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//}





package com.maarco.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations including generation, parsing and validation.
 * Handles all JWT-related functionality for the authentication system.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Token generation with configurable expiration (default: 7 days)</li>
 *   <li>Token parsing and claims extraction</li>
 *   <li>Token validation against user details</li>
 *   <li>Expiration checking</li>
 * </ul>
 *
 * <p>Uses HMAC-SHA512 algorithm for token signing with a secret key.</p>
 */
@Component
public class JwtHelper {

    // Token validity set to 7 days (in seconds)
    public static final long JWT_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    // Secret key for signing and verifying tokens
    private final String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";

    /**
     * Retrieves username from JWT token
     * @param token JWT token string
     * @return Username extracted from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieves expiration date from JWT token
     * @param token JWT token string
     * @return Expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Generic method to retrieve any claim from token
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Requested claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Retrieves all claims from token using secret key
     * @param token JWT token string
     * @return All claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Checks if token has expired
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Generates token for user with default claims
     * @param userDetails User details to include in token
     * @return Generated JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * Internal method for token generation
     * @param claims Additional claims to include
     * @param subject Token subject (typically username)
     * @return Generated JWT token string
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * Validates token against user details
     * @param token JWT token to validate
     * @param userDetails User details to validate against
     * @return true if token is valid for given user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}