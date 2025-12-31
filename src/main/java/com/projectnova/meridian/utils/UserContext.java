package com.projectnova.meridian.utils;

import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {


    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(()
                -> new RuntimeException("User not found"));
    }

    public Long getCurrentOrganizationId() {
        return getCurrentUser().getOrganization().getId();
    }
}
