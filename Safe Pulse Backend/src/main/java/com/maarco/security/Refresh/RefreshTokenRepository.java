package com.maarco.security.Refresh;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing RefreshToken entities in MongoDB.
 * Provides methods to store, retrieve, and manage refresh tokens used in JWT authentication.
 *
 * <p>This repository supports the token refresh workflow by allowing lookup of tokens
 * when generating new access tokens.</p>
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token string value
     * @param token The refresh token string to search for
     * @return Optional containing the matching RefreshToken if found
     */
    Optional<RefreshToken> findByRefreshToken(String token);
}