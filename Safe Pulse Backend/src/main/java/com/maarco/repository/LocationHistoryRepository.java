package com.maarco.repository;


import com.maarco.entities.LocationHistory;
import com.maarco.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationHistoryRepository extends MongoRepository<LocationHistory, String> {
//    List<LocationHistory> findByUser_UserId(String userId);

    List<LocationHistory> findByUser(User user);
    List<LocationHistory> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    List<LocationHistory> findByUserAndTimestampBetweenOrderByTimestampAsc(User user, LocalDateTime start, LocalDateTime end);
}
