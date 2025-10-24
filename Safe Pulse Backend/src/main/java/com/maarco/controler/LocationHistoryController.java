package com.maarco.controler;


import com.maarco.dtos.LocationHistoryDTO;
import com.maarco.entities.LocationHistory;
import com.maarco.entities.User;
import com.maarco.repository.LocationHistoryRepository;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationHistoryController {

//    @Autowired
//    private LocationHistoryService locationHistoryService;

    @PostMapping("/save")
    public LocationHistoryDTO saveLocation(@RequestParam String email,
                                           @RequestParam Double latitude,
                                           @RequestParam Double longitude) {
        LocationHistory savedLocation = saveLocation1(email, latitude, longitude);
        return new LocationHistoryDTO(savedLocation.getId(), savedLocation.getLatitude(), savedLocation.getLongitude());
    }


//    @GetMapping("/user/{userId}")
//    public List<LocationHistory> getUserLocations(@PathVariable String userId) {
//        return locationHistoryService.getUserLocations(userId);
//    }


    @Autowired
    private LocationHistoryRepository locationHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public LocationHistory saveLocation1(String email, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        LocationHistory locationHistory = new LocationHistory();
        locationHistory.setLatitude(latitude);
        locationHistory.setLongitude(longitude);
//        locationHistory.setUser(user);

        return locationHistoryRepository.save(locationHistory);
    }

//    public List<LocationHistory> getUserLocations(String userId) {
//        return locationHistoryRepository.findByUser_UserId(userId);
//    }
}
