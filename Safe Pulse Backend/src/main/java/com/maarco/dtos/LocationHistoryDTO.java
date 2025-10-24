package com.maarco.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LocationHistoryDTO {
    private String id;
    private Double latitude;
    private Double longitude;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public LocationHistoryDTO(String id, Double latitude, Double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
