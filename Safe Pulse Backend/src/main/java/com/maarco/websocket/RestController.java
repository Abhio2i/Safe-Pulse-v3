package com.maarco.websocket;

import com.maarco.entities.LocationHistory;
import com.maarco.entities.User;
import com.maarco.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/rest")
public class RestController {
    @Autowired
    private LocationHistoryService locationHistoryService;

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository

//    @PostMapping("/bulk-location-save")
//    public ResponseEntity<String> saveBulkLocations(
//            @RequestParam String username,
//            @RequestBody List<LocationHistory> locationHistories) {
//
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + username));
//
//        for (LocationHistory locationHistory : locationHistories) {
//            locationHistory.setUser(user);
//        }
//
//        locationHistoryService.saveAllLocations(locationHistories);
////        locationHistoryService.saveAllLocationsDemo(locationHistories);
//
//        return ResponseEntity.ok("Data is successfully saved");
//    }


@PostMapping("/bulk-location-save")
public ResponseEntity<String> saveBulkLocations(
        @RequestParam String username,
        @RequestBody List<LocationHistory> locationHistories) {

    User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + username));

    for (LocationHistory locationHistory : locationHistories) {
        locationHistory.setUser(user);
    }

    locationHistoryService.saveAllLocations(locationHistories);
    // This will now work because we've modified the method
    locationHistoryService.saveAllLocationsDemo(locationHistories);

    return ResponseEntity.ok("Data is successfully saved");
}
}
