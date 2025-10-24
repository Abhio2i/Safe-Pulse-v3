/**
 * Service class for managing refresh token operations in JWT authentication.
 * Handles creation, validation, and expiration of refresh tokens with a default
 * validity period of 14 days. Maintains synchronization between User entities
 * and their corresponding refresh tokens.
 */

package com.maarco.security.Refresh;

import com.maarco.entities.User;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

//    public long refreshTokenValidity = 5*60*60*1000;

    public long refreshTokenValidity = 14 * 24 * 60 * 60 * 1000; // 7 days in milliseconds


//    public long refreshTokenValidity = 2*60*1000;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository ;

    public RefreshToken createRefreshToken(String userName) {

        User user = userRepository.findByEmail(userName).get();
        RefreshToken refreshToken1 = user.getRefreshToken();

        if (refreshToken1==null){
            refreshToken1 =  RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString())
                    .expiry(Instant.now().plusMillis(refreshTokenValidity))
                    .user(userRepository.findByEmail(userName).get())
                    .build();

        }else {
            refreshToken1.setExpiry(Instant.now().plusMillis(refreshTokenValidity));
        }

        user.setRefreshToken(refreshToken1);


       refreshTokenRepository.save(refreshToken1);

        return refreshToken1;
    }

    public RefreshToken verifyRefreshToken(String refreshToken){

        RefreshToken refreshTokenOb = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(()-> new RuntimeException("Given Token Does Not Expsist"));

        if (refreshTokenOb.getExpiry().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshTokenOb);
            throw new RuntimeException("Refresh Token Expired !!");

        }
        return refreshTokenOb;

    }




}
