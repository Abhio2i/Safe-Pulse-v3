package com.maarco.emergencyContacts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyContactDto {

    private String  name;
    private String relation;
    private String number;
    private String imageUrl;

}
