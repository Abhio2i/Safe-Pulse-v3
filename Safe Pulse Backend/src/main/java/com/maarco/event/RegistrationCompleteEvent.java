/**
 * Custom Spring application event that gets triggered when a user completes registration.
 * Carries the registered user and application context URL for verification purposes.
 * <p>
 * Extends Spring's ApplicationEvent to integrate with Spring's event publishing system.
 */


package com.maarco.event;

import com.maarco.entities.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private User user;
    private String applicationUrl;

    //runs when registration event is completed
    public RegistrationCompleteEvent(User user, String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
    }
}
