package com.maarco.emergencyContacts;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmergencyContactsRepository extends MongoRepository<EmergencyContacts, String> {
    EmergencyContacts findByUserId(String userId);
}