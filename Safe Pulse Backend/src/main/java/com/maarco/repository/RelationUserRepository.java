package com.maarco.repository;

import com.maarco.entities.LocationHistory;
import com.maarco.entities.RelationUser;
import com.maarco.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing user relationships in MongoDB.
 * Provides methods to query and manage relationships between users including:
 * - Finding relationships by users (either as source or target)
 * - Checking relationship existence and status
 * - Querying linked relationships
 */
public interface RelationUserRepository extends MongoRepository<RelationUser, String> {

    /**
     * Finds all relationships where either user is the source or target
     * @param fromUser The source user in the relationship
     * @param toUser The target user in the relationship
     * @return List of matching relationships
     */
    List<RelationUser> findByFromUserOrToUser(User fromUser, User toUser);

    /**
     * Finds all relationships where the specified user is the target
     * @param toUser The target user in the relationships
     * @return List of relationships where the user is the target
     */
    List<RelationUser> findByToUser(User toUser);

    /**
     * Checks if a specific relationship exists between users
     * @param fromUser The source user
     * @param toUser The target user
     * @param relationName The name/type of relationship
     * @return true if the relationship exists, false otherwise
     */
    boolean existsByFromUserAndToUserAndRelationName(User fromUser, User toUser, String relationName);

    /**
     * Finds relationships where either user is involved and matches the link status
     * @param fromUser The source user
     * @param toUser The target user
     * @param isLinked The link status to match (1.0 = linked, 0.0 = not linked)
     * @return List of matching relationships
     */
    List<RelationUser> findByFromUserOrToUserAndIsLinked(User fromUser, User toUser, Double isLinked);

    /**
     * Finds all relationships targeting a specific user by their ID
     * @param userId The ID of the target user
     * @return List of relationships targeting the user
     */
    List<RelationUser> findByToUserUserId(String userId);

    /**
     * Finds a specific relationship between two users
     * @param fromUser The source user
     * @param toUser The target user
     * @return Optional containing the relationship if found
     */
    Optional<RelationUser> findByFromUserAndToUser(User fromUser, User toUser);

    /**
     * Finds an active (linked) relationship between two users by their IDs
     * @param fromUserId The ID of the source user
     * @param toUserId The ID of the target user
     * @return Optional containing the linked relationship if found
     */
    @Query("{ 'fromUser.$id': ?0, 'toUser.$id': ?1, 'isLinked': 1.0 }")
    Optional<RelationUser> findLinkedRelationship(String fromUserId, String toUserId);

    /**
     * Finds all linked relationships originating from a specific user
     * @param fromUser The source user
     * @param isLinked The link status (1.0 for linked relationships)
     * @return List of linked relationships from the user
     */
    List<RelationUser> findByFromUserAndIsLinked(User fromUser, double isLinked);

    // Commented out example query for reference:
    // @Query(value = "SELECT * FROM location_history WHERE user_id = :userId AND DATE(timestamp) = :date ORDER BY timestamp", nativeQuery = true)
    // List<LocationHistory> findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}