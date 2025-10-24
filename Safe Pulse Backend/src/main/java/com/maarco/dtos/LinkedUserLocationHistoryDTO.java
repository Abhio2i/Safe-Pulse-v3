package com.maarco.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class LinkedUserLocationHistoryDTO {
//    private String userEmail;
//    private String userName;
    private List<LocationHistoryDTO> locations;

    // getters and setters
}
