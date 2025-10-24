/**
 * The RelationUser class represents relationships between users in the system,
 * storing how two users are connected and the nature of their relationship.
 * <p>
 * **Key Features:**
 * - Defines relationships between two users (fromUser → toUser)
 * - Stores relationship type (relationName) like family/friend connections
 * - Includes a numeric strength indicator (isLinked) for the relationship
 * - Uses MongoDB references (@DBRef) to link User documents
 * <p>
 * **Common Use Cases:**
 * - Family tree connections (parent-child, siblings)
 * - Social network friend relationships
 * - Professional connections in business apps
 * - Relationship strength tracking
 */
package com.maarco.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter

@Setter

@Document(collection = "RelationUser")
public class RelationUser {

    @Id
    private String relationId;
    private Double isLinked;
    private String relationName;  // like Brother, Sister, Son, Father etc

    @DBRef
    private User fromUser;

    @DBRef
    private User toUser;

    public RelationUser() {
    }

    public RelationUser(String relationId, Double isLinked, String relationName, User fromUser, User toUser) {
        this.relationId = relationId;
        this.isLinked = isLinked;
        this.relationName = relationName;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

}