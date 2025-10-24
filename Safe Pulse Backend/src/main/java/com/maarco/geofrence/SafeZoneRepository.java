package com.maarco.geofrence;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SafeZoneRepository extends MongoRepository<SafeZone, String> {
    List<SafeZone> findByCreatedBy(String email);
    List<SafeZone> findBySharedWithContaining(String email);

    List<SafeZone> findByCreatedByOrSharedWithContaining(String email, String email1);

//    List<SafeZone> findByCreatedByOrSharedWithContaining(String createdBy, String sharedWith);
//    List<SafeZone> findBySharedWithContaining(String sharedWith);
}