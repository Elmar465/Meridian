package com.projectnova.meridian.controller;

import com.projectnova.meridian.dao.OrganizationRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.DuplicateResourceException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.Organization;
import com.projectnova.meridian.model.OrganizationStatus;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
import com.projectnova.meridian.service.EmailService;
import com.projectnova.meridian.service.JwtService;
import com.projectnova.meridian.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("=== LOGIN ATTEMPT ===");
        log.info("Username: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.info("Authentication successful");

            User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(()
                    -> new ResourceNotFoundException("Username not found"));

            log.info("User found: {}", user.getUsername());
            log.info("User ID: {}", user.getId());
            log.info("Organization: {}", user.getOrganization());

            // Check if user has organization
            if (user.getOrganization() == null) {
                log.error("User has no organization!");
                throw new ResourceNotFoundException("User is not part of any organization");
            }

            log.info("Organization ID: {}", user.getOrganization().getId());
            log.info("Organization Name: {}", user.getOrganization().getName());

            String jwt = jwtService.generateToken(user);
            log.info("JWT generated successfully");

            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    jwt,
                    user.getUsername(),
                    user.getRole(),
                    user.getOrganization().getId(),
                    user.getOrganization().getName(),
                    user.getOrganization().getSlug()
            ));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        User findUser = userRepository.findByUsername(request.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("Username not found"));
        String jwt = jwtService.generateToken(findUser);
        emailService.sendWelcomeEmail(findUser);
        return ResponseEntity.ok(new AuthResponse(
                findUser.getId(),
                jwt,
                findUser.getUsername(),
                findUser.getRole(),
                findUser.getOrganization().getId(),
                findUser.getOrganization().getName(),
                findUser.getOrganization().getSlug()
        ));
    }

    @PostMapping("/setup")
    @Transactional
    public ResponseEntity<AuthResponse> setupOrganization(@Valid @RequestBody SetupOrganizationRequest request) {
        log.info("=== SETUP ATTEMPT ===");
        log.info("Organization Name: {}", request.getOrganizationName());
        log.info("Username: {}", request.getUsername());
        log.info("Email: {}", request.getEmail());

        try {
            // Check for duplicate username
            if (userRepository.existsByUsername(request.getUsername())) {
                log.error("Username already exists: {}", request.getUsername());
                throw new DuplicateResourceException("Username already exists");
            }

            // Check for duplicate email
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("Email already exists: {}", request.getEmail());
                throw new DuplicateResourceException("Email already exists");
            }

            log.info("Creating user...");

            // 1. Create the User first (without organization)
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.ADMIN);
            user.setIsActive(true);

            User savedUser = userRepository.save(user);
            log.info("User created with ID: {}", savedUser.getId());

            log.info("Creating organization...");

            // 2. Create the Organization with user as owner
            Organization organization = new Organization();
            organization.setName(request.getOrganizationName());
            organization.setDescription(request.getOrganizationDescription());
            organization.setSlug(generateUniqueSlug(request.getOrganizationName()));
            organization.setOwner(savedUser);
            organization.setStatus(OrganizationStatus.ACTIVE);

            Organization savedOrg = organizationRepository.save(organization);
            log.info("Organization created with ID: {}", savedOrg.getId());

            // 3. Update user with organization reference
            savedUser.setOrganization(savedOrg);
            userRepository.save(savedUser);
            log.info("User updated with organization");

            // 4. Generate JWT token
            String jwt = jwtService.generateToken(savedUser);
            log.info("JWT generated");

            // 5. Send welcome email (non-blocking)
            try {
                emailService.sendWelcomeEmail(savedUser);
                log.info("Welcome email sent");
            } catch (Exception e) {
                log.warn("Failed to send welcome email: {}", e.getMessage());
            }

            log.info("=== SETUP COMPLETE ===");

            return ResponseEntity.ok(new AuthResponse(
                    savedUser.getId(),
                    jwt,
                    savedUser.getUsername(),
                    savedUser.getRole(),
                    savedOrg.getId(),
                    savedOrg.getName(),
                    savedOrg.getSlug()
            ));
        } catch (Exception e) {
            log.error("Setup failed: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (baseSlug.isEmpty()) {
            baseSlug = "workspace";
        }

        String slug = baseSlug;
        int counter = 2;
        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
}