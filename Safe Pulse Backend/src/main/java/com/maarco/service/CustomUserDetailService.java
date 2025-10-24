package com.maarco.service;

import com.maarco.entities.User;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user-specific data during authentication by querying the UserRepository.
 * Translates between application's User entity and Spring Security's UserDetails.
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found !!"));
        return user;
    }
}
