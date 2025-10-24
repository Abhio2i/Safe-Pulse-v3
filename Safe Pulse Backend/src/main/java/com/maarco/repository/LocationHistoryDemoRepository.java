package com.maarco.repository;
/**
 * MongoDB repository interface for managing LocationHistoryDemo entities.
 * Extends MongoRepository to provide basic CRUD operations and query methods
 * for LocationHistoryDemo documents with String as the ID type.
 * <p>
 * This repository can be used to perform operations like save, find, delete etc.
 * on LocationHistoryDemo entities without requiring explicit implementation.
 */

import com.maarco.entities.LocationHistory;
import com.maarco.entities.LocationHistoryDemo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationHistoryDemoRepository extends MongoRepository<LocationHistoryDemo, String> {


}
