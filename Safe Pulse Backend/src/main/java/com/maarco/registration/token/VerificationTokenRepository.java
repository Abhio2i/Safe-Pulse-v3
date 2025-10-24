package com.maarco.registration.token;

import com.maarco.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
/**
 * Repository interface for managing VerificationToken entities in MongoDB.
 * Provides methods for finding tokens by token string or associated user,
 * and deleting tokens by user ID.
 */
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    VerificationToken findByUser(User user);
    void deleteByUserUserId(Long userId);

}
