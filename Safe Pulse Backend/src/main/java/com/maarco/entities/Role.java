/**
 * The Role class represents a basic role entity in the system,
 * typically used for authorization and access control purposes.
 * <p>
 * **Key Features:**
 * - Simple role definition with just an ID and name
 * - Maps to MongoDB collection (default collection name would be "role")
 * - Uses Lombok's @Data for automatic getters/setters/toString
 * <p>
 * **Common Use Cases:**
 * - User role management (ADMIN, USER, MODERATOR etc.)
 * - Role-based access control (RBAC) implementation
 * - Permission grouping
 */

package com.maarco.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Role {
    @Id
    public int id;
    public String name;
}
