package com.projectnova.meridian.controller;


import com.projectnova.meridian.dao.MessageRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.ConversationResponse;
import com.projectnova.meridian.dto.MessageResponse;
import com.projectnova.meridian.dto.SendMessageRequest;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;


    @PutMapping("/{userId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails)
    {
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            messageService.markAsRead(userId, currentUser);
            return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Long count  = messageService.getUnreadCount(currentUser);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody @Valid SendMessageRequest sendMessageRequest,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
       MessageResponse messageResponse =  messageService.sendMessage(sendMessageRequest, currentUser);
        return new ResponseEntity<>(messageResponse,  HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<MessageResponse>> getConversation(@PathVariable Long userId,
                                                                 @AuthenticationPrincipal UserDetails userDetails,
                                                                 Pageable pageable) {
        User curentUser =  userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Page<MessageResponse> messageResponse = messageService.getConversation(curentUser, userId, pageable);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationResponse>> getConversations(@AuthenticationPrincipal UserDetails userDetails,
                                                                       Pageable pageable) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Page<ConversationResponse> messageResponse = messageService.getConversations(currentUser, pageable);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
