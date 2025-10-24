    package com.maarco.websocket;


import com.maarco.entities.LocationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryyRepository extends MongoRepository<LocationHistory, String> {

    List<LocationHistory> findTop50ByUserUserIdOrderByTimestampDesc(String userId);


    List<LocationHistory> findByUserUserIdAndTimestampAfterOrderByTimestampDesc(String userId, LocalDateTime timestamp);

}