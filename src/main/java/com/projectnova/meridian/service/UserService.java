package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.OrganizationRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.BadRequestException;
import com.projectnova.meridian.exceptions.DuplicateResourceException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.Organization;
import com.projectnova.meridian.model.OrganizationStatus;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB for avatars
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private final OrganizationRepository organizationRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadAvatar(MultipartFile file, User currentUser) throws IOException {
        if(file == null || file.isEmpty()) {
            throw new BadRequestException("Please  Select a file ");
        }
        String fileName = file.getOriginalFilename();
        String  contentType = file.getContentType();
        if(!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new BadRequestException("Only images files allowed " + contentType);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileName;
        Path uploadPath = Paths.get(uploadDir + "/avatars");
        Path targetPath = uploadPath.resolve(uniqueFileName);
        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        String avatarUrl = "/uploads/avatars/" + uniqueFileName;
        currentUser.setAvatar(avatarUrl);
        userRepository.save(currentUser);
        return avatarUrl;
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest, User currentUser) {
        if (!Objects.equals(changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmPassword())) {
            throw new ResourceNotFoundException("New Password or Confirm Password Mismatch");
        }
        if(!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            throw new ResourceNotFoundException("Current password is incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);
    }

    public Page<UserResponse> getAllUsers(User currentUser,Pageable pageable) {
        Long  orgId = currentUser.getOrganization().getId();
        Page<User> users = userRepository.findByOrganizationId(orgId,pageable);
        return users.map(this::convertToUserResponse);
    }

    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        Page<User> users = userRepository.findByIsActive(true,  pageable);
        return users.map(this::convertToUserResponse);
    }

    public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByRole(role,  pageable);
        return users.map(this::convertToUserResponse);
    }

    @Transactional
    public void deleteUser(Long id, User currentUser) throws AccessDeniedException {
        Long orgId = currentUser.getOrganization().getId();

        User existingUser = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User not found" + id));
        if(!orgId.equals(existingUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access Denied");
        }
        userRepository.delete(existingUser);
    }


    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, User currentUser) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        Long orgId = currentUser.getOrganization().getId();
        if(!orgId.equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        updateEntity(user, request);
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }



    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::convertToUserResponse);
    }


    public Page<UserResponse> filterUsers(UserRole role,  Boolean isActive, Pageable  pageable) {
        Page<User> users = userRepository.filterUsers(role, isActive, pageable);
        return users.map(this::convertToUserResponse);
    }


    @Transactional
    public UserResponse updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);

        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if(existsByUsername(createUserRequest.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if(existsByEmail(createUserRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        long userCounbt = userRepository.count();
        UserRole assignRole = (userCounbt == 0) ? UserRole.ADMIN : UserRole.MEMBER;

       String orgName  = generateOrganizationName(createUserRequest.getFirstName());
       String baseSlug = generateSlug(orgName);
       String uniqueSlug = generateUniqueSlug(baseSlug);

        //Convert Dto -> Entity
        User user = convertToEntity(createUserRequest);
        user.setRole(assignRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        Organization  org =  new Organization();
        org.setName(orgName);
        org.setSlug(uniqueSlug);
        org.setOwner(savedUser);
        org.setStatus(OrganizationStatus.ACTIVE);
        Organization savedOrg = organizationRepository.save(org);
        savedUser.setOrganization(savedOrg);
        userRepository.save(savedUser);
        return convertToUserResponse(savedUser);
    }


    private String generateUniqueSlug(String baseSlug){
        String slug = baseSlug;
        int counter = 2;

        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String generateSlug(String name) {
        if(name == null || name.isEmpty()) {
            return "workspace";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replace(" ", "-");
    }

    private String generateOrganizationName(String firstName) {
       if(firstName == null || firstName.trim().isEmpty()){
           return "My WorkSpace";
       }
       return firstName + "'s WorkSpace";
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email  not found" + email));

        return convertToUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).
            orElseThrow(() -> new ResourceNotFoundException("Username not found" + username));
        return convertToUserResponse(user);
    }

    public UserResponse getUserById(Long id, User currentUser) throws AccessDeniedException {
       User user = userRepository.findById(id).
               orElseThrow(()  -> new ResourceNotFoundException("User not found with id" + id));
       Long orgId = currentUser.getOrganization().getId();
       if(!orgId.equals(user.getOrganization().getId())) {
           throw new AccessDeniedException("Access Denied");
       }
       return convertToUserResponse(user);
    }

    public List<UserResponse> getActiveUsers() {
        return convertToResponseList(userRepository.findByIsActiveTrue());
    }


    public List<UserResponse> getUsersByRole(UserRole role) {
        return convertToResponseList(userRepository.findByRole(role));
    }


    public List<UserResponse> getAllUsers() {
        return
                convertToResponseList(userRepository.findAll());
    }


    public boolean existsByUsername(String username) {

        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private List<UserResponse> convertToResponseList(List<User> users){
            return users.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
    }

    public UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

   private User convertToEntity(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setPassword(createUserRequest.getPassword());
        user.setRole(createUserRequest.getRole() != null ? createUserRequest.getRole() : UserRole.MEMBER);
        user.setIsActive(true);
        return user;
   }

   private User updateEntity(User user, UpdateUserRequest  request)  {

        if(request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if(request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if(request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if(request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return user;
   }
}
