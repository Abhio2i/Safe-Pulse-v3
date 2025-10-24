package com.maarco.geofrence;//package com.maarco.geofrence;
//
//
//import lombok.*;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.List;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Document(collection = "safe_zones")
//public class SafeZone {
//    @Id
//    private String id;
//
//    private String name;
//    private Double latitude;
//    private Double longitude;
//    private Double radius;
//    private String type; // "safe" or other types
//    private String createdBy; // email of creator
//
//    private List<String> sharedWith; // list of emails
//    private SosSettings sosSettings;
//
//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class SosSettings {
//        private boolean alert;
//        private boolean call;
//        private boolean notification;
//        private boolean message;
//    }
//}




import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Document(collection = "safe_zones")
public class SafeZone {
    @Id
    private String id;

    private String name;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private ZoneType type; // Enum: SAFE or DANGER
    private String createdBy; // email of creator
    private List<String> sharedWith; // list of emails
    private SosSettings sosSettings;

    public enum ZoneType {
        SAFE, DANGER
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class SosSettings {
        private boolean alert;
        private boolean call;
        private boolean notification;
        private boolean message;
    }
}