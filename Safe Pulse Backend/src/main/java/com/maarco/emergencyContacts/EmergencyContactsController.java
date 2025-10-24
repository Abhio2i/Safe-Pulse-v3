package com.maarco.emergencyContacts;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactsController {

    @Autowired
    private EmergencyContactsService service;

    @GetMapping("/{userId}")
    public EmergencyContacts getContacts(@PathVariable String userId) {
        return service.getContactsForUser(userId);
    }

    @PutMapping("/{userId}")
    public EmergencyContacts updateContacts(
            @PathVariable String userId,
            @RequestBody EmergencyContacts contacts) {
        return service.updateContactsForUser(userId, contacts);
    }
}
