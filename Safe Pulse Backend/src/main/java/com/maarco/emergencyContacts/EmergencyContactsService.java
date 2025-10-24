package com.maarco.emergencyContacts;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmergencyContactsService {

    @Autowired
    private EmergencyContactsRepository repository;

    public EmergencyContacts getDefaultContacts() {
        return new EmergencyContacts();
    }

    public EmergencyContacts getContactsForUser(String userId) {
        EmergencyContacts contacts = repository.findByUserId(userId);
        return contacts != null ? contacts : getDefaultContacts();
    }

    public EmergencyContacts updateContactsForUser(String userId, EmergencyContacts newContacts) {
        EmergencyContacts existing = repository.findByUserId(userId);

        if (existing == null) {
            newContacts.setUserId(userId);
            return repository.save(newContacts);
        }

        existing.setAmbulance(newContacts.getAmbulance());
        existing.setPolice(newContacts.getPolice());
        existing.setFire(newContacts.getFire());
        existing.setEmergency(newContacts.getEmergency());

        return repository.save(existing);
    }
}