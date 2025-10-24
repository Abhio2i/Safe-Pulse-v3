package com.maarco.websocket;


import com.maarco.entities.LocationHistory;
import com.maarco.entities.LocationHistoryDemo;
import com.maarco.repository.LocationHistoryDemoRepository;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class LocationHistoryService {

    @Autowired
    private LocationHistoryyRepository locationHistoryRepository;
    @Autowired
    private LocationHistoryDemoRepository locationHistoryRepositorydemo;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  SimpMessagingTemplate messagingTemplate;
    // In LocationHistoryService.java

    private final ConcurrentMap<String, LocalDateTime> lastUpdateTimes = new ConcurrentHashMap<>();
    private static final long OFFLINE_THRESHOLD_MINUTES = 1;

//    // Track last update times
//    @Scheduled(fixedRate = 60000) // Check every minute
//    public void checkUserStatus() {
//        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);
//
//        lastUpdateTimes.forEach((userId, lastUpdate) -> {
//            if (lastUpdate.isBefore(threshold)) {
//                // User hasn't sent updates in threshold time - mark as offline
//                messagingTemplate.convertAndSend("/topic/user-status",
//                        new UserStatus(userId, false));
//                lastUpdateTimes.remove(userId);
//            }
//        });
//    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(OFFLINE_THRESHOLD_MINUTES);

        lastUpdateTimes.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(threshold)) {
                // User is offline
                messagingTemplate.convertAndSend("/topic/user-status",
                        new UserStatus(entry.getKey(), false, entry.getValue().toString()));
                return true;
            }
            return false;
        });
    }
//    public LocationHistory saveLocation(LocationHistory locationHistory) {
//        String userId = locationHistory.getUser().getUserId();
//        lastUpdateTimes.put(userId, LocalDateTime.now());
//
//        // Notify that user is online
//        messagingTemplate.convertAndSend("/topic/user-status",
//                new UserStatus(userId, true));
//
//        return locationHistoryRepository.save(locationHistory);
//    }


    public LocationHistory saveLocation(LocationHistory locationHistory) {
        String userId = locationHistory.getUser().getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Update last seen time
        lastUpdateTimes.put(userId, now);
        locationHistory.setTimestamp(now);

        System.out.printf("-----------aytsaydtgaud-------------");

        // Notify online status with timestamp
        messagingTemplate.convertAndSend("/topic/user-status",
                new UserStatus(userId, true, now.toString()));

        return locationHistoryRepository.save(locationHistory);
    }
//    public LocationHistory saveLocation(LocationHistory locationHistory) {
//        return locationHistoryRepository.save(locationHistory);
//    }

    public List<LocationHistory> findLast50LocationsByUserId(String userId) {
        return locationHistoryRepository.findTop50ByUserUserIdOrderByTimestampDesc(userId);
    }

    public List<LocationHistory> findLocationsFromLast2HoursByUserId(String userId) {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        return locationHistoryRepository.findByUserUserIdAndTimestampAfterOrderByTimestampDesc(userId, twoHoursAgo);
    }
    public List<LocationHistory> saveAllLocations(List<LocationHistory> locationHistories) {
        return locationHistoryRepository.saveAll(locationHistories);
    }

    public List<LocationHistoryDemo> saveAllLocationsDemo(List<LocationHistory> locationHistories) {
        List<LocationHistoryDemo> demoList = locationHistories.stream()
                .map(this::convertToDemo)
                .collect(Collectors.toList());
        return locationHistoryRepositorydemo.saveAll(demoList);
    }

    private LocationHistoryDemo convertToDemo(LocationHistory locationHistory) {
        LocationHistoryDemo demo = new LocationHistoryDemo();
        demo.setLatitude(locationHistory.getLatitude());
        demo.setLongitude(locationHistory.getLongitude());
        demo.setTimestamp(locationHistory.getTimestamp());
        demo.setUser(locationHistory.getUser());
        return demo;
    }

}