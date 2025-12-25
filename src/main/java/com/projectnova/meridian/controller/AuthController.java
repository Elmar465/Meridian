package com.projectnova.meridian.controller;


import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.AuthResponse;
import com.projectnova.meridian.dto.CreateUserRequest;
import com.projectnova.meridian.dto.LoginRequest;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.service.EmailService;
import com.projectnova.meridian.service.JwtService;
import com.projectnova.meridian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService  jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        User user  =  userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("Username not found"));

        String jwt = jwtService.generateToken(user);
        return  ResponseEntity.ok(new AuthResponse(
                user.getId(),
                jwt,
                user.getUsername(),
                user.getRole()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        User findUser = userRepository.findByUsername(request.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("Username not found"));
        String jwt = jwtService.generateToken(findUser);
        emailService.sendWelcomeEmail(findUser);
        return  ResponseEntity.ok(new AuthResponse(
                findUser.getId(),
                jwt,
                findUser.getUsername(),
                findUser.getRole()
        ));
    }
}
