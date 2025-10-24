package com.maarco.dtos;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RelationUserDTO {
//    private String relationId;
//    private Double isLinked;
//    private String relationName;
//    private String email;
//    private boolean isFromUser;  // true if current user sent the request


    private String relationId;
    private Double isLinked;
    private String relationName;
    private String otherUserEmail;
    private String userRelationId;
    private String relationDirection; // "incoming" or "outgoing"
    private String activityStatus; // "Active" or "Not Active"
    private String imageUrl;



//    public RelationUserDTO(String relationId, Double isLinked, String relationName, String otherUserEmail, boolean isFromUser) {
//    }

//
//    public RelationUserDTO(String relationId, Double isLinked, String relationName, String email) {
//        this.relationId = relationId;
//        this.isLinked = isLinked;
//        this.relationName = relationName;
//        this.email = email;
//    }
}
