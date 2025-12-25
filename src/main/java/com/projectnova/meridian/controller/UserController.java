package com.projectnova.meridian.controller;

import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.ChangePasswordRequest;
import com.projectnova.meridian.dto.CreateUserRequest;
import com.projectnova.meridian.dto.UpdateUserRequest;
import com.projectnova.meridian.dto.UserResponse;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
import com.projectnova.meridian.service.UserService;
import com.projectnova.meridian.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserContext userContext;


    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                               @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
      String avatarUrl =   userService.uploadAvatar(file,user);
        return ResponseEntity.ok(avatarUrl);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword (@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                                @AuthenticationPrincipal UserDetails   userDetails) {
        User user  = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        userService.changePassword(changePasswordRequest, user);
        return new  ResponseEntity<>(HttpStatus.OK);

    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request){
        Long userId = userContext.getCurrentUserId();
        User currentUser =userRepository.findById(userId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(!id.equals(userId) && !currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new RuntimeException("You can only update your own account unless you are an admin");
        }
        UserResponse userResponse = userService.updateUser(id, request);
        return new  ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String searchTerm, Pageable pageable) {
        Page<UserResponse> users = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long userId = userContext.getCurrentUserId();
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/filter")
    public ResponseEntity<Page<UserResponse>> filterUsers(@RequestParam(required = false) UserRole role,
                                                          @RequestParam(required = false ) Boolean isActive,
                                                          Pageable pageable) {
        Page<UserResponse> users = userService.filterUsers(role, isActive, pageable);
        return  ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> roleUpdate) {

        String newRole = roleUpdate.get("role");
        UserResponse updatedUser = userService.updateUserRole(id, UserRole.valueOf(newRole));
        return ResponseEntity.ok(updatedUser);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request){
       UserResponse userResponse = userService.createUser(request);
       return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }




    @GetMapping("/active")
    public ResponseEntity<Page<UserResponse>> getActiveUsers(Pageable pageable){
        return  ResponseEntity.ok(userService.getActiveUsers(pageable));
    }


    @GetMapping("/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole (@PathVariable UserRole role, Pageable pageable) {
        Page<UserResponse> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse>  getUserByEmail(@PathVariable String email){
        UserResponse  userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user  = userService.getUserByUsername(username);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MEMBER')")
    ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
