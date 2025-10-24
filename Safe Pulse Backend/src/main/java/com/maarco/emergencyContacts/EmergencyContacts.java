package com.maarco.emergencyContacts;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emergency_contacts")
public class EmergencyContacts {
    @Id
    private String id;
    private String ambulance = "102";
    private String police = "100";
    private String fire = "101";
    private String emergency = "112";

    // You can add user reference if you want to track which user these belong to
    private String userId;
}