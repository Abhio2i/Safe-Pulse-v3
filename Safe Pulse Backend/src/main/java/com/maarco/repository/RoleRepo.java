package com.maarco.repository;

import com.maarco.entities.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing Role entities in MongoDB.
 * Provides basic CRUD operations for Role management.
 * Uses Integer as the ID type for Role entities.
 * <p>
 * Extends MongoRepository which provides built-in methods like:
 * - save(S entity)
 * - findById(ID id)
 * - findAll()
 * - deleteById(ID id)
 * - count()
 * etc.
 */
public interface RoleRepo extends MongoRepository<Role, Integer> {
    // Basic CRUD operations are inherited from MongoRepository
    // Custom query methods can be added here as needed
}