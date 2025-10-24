package com.maarco.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class NotifySendSuccessDTO {


    private Long id;
    private LocalDate localDate;
    private LocalTime startTime;
//    private String desc;
    private String body;
    private String message;  // Add this field for the message

    // Add a setter method for the message
    public void setMessage(String message) {
        this.message = message;
    }
}
