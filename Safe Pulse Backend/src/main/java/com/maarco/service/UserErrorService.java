package com.maarco.service;


import com.maarco.repository.UserErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for handling user error operations.
 * Provides functionality for logging and managing user-related errors
 * through interaction with the UserErrorRepository.
 */
@Service
public class UserErrorService {

    private final UserErrorRepository userErrorRepository;

    @Autowired
    public UserErrorService(UserErrorRepository userErrorRepository) {
        this.userErrorRepository = userErrorRepository;
    }

}
