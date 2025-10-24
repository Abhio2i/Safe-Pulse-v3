/**
 * The User class represents a core user entity in the system, implementing Spring Security's UserDetails
 * for authentication and authorization purposes. It serves as the central user profile with comprehensive
 * attributes including security, location, relationships, and notification capabilities.
 * <p>
 * **Key Features:**
 * - Complete user management with authentication (UserDetails implementation)
 * - Geolocation tracking (latitude/longitude)
 * - Email verification and session management
 * - Role-based authorization (connected to Role entities)
 * - Relationship management (via RelationUser)
 * - Notification system integration
 * - User profile linkage
 * - Refresh token support for JWT authentication
 * <p>
 * **Security Implementation:**
 * - Implements all UserDetails methods for Spring Security integration
 * - Manages granted authorities through Role assignments
 * - Controls account status via email verification flag
 */
package com.maarco.entities;

import com.maarco.registration.token.VerificationToken;
import com.maarco.security.Refresh.RefreshToken;
import com.fasterxml.jackson.annotation.*;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
//@Entity
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String userId;

    private String userName;
    private String email;
    private String password;
    private String mobileNo;
    private String deviceType;
    private Double latitude;
    private Double longitude;
    private String address;

    private LocalDate localDate = LocalDate.now();
    private boolean emailVerified = false;
    private String notificationToken;


    private boolean connected = false;
    private LocalDateTime lastSeen;
    private String currentSessionId;

    @DBRef
    @JsonIgnore
    private RefreshToken refreshToken;

    private LocalDateTime registrationTimestamp = LocalDateTime.now();


    @DBRef
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @DBRef
    private Set<Role> roles = new HashSet<>();

    @DBRef
    @JsonBackReference("userReference")
    private UserProfile userProfile;

    @DBRef
    private List<RelationUser> relations = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authories = this.roles.stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        return authories;
    }

    public String getUserName() {
        return this.userName;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.emailVerified;
    }

    @DBRef
    private List<NotificationEntity> notifications;

    public String getNotificationToken() {
        return this.notificationToken;
    }

    public void updateNotificationToken(String newToken) {
        if (newToken != null && (this.notificationToken == null || !this.notificationToken.equals(newToken))) {
            this.notificationToken = newToken;
        }
    }
}
