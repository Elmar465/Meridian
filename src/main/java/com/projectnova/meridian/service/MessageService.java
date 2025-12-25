package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.MessageRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.ConversationResponse;
import com.projectnova.meridian.dto.MessageResponse;
import com.projectnova.meridian.dto.SendMessageRequest;
import com.projectnova.meridian.exceptions.BadRequestException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.Message;
import com.projectnova.meridian.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    public void markAsRead(Long otherUserId, User currentUser) {
         messageRepository.markAsRead(otherUserId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(User currentUser) {
        try {
            System.out.println("=== GET UNREAD COUNT ===");
            Long count = messageRepository.countByReceiverAndIsReadFalse(currentUser);
            System.out.println("Unread count: " + count);
            return count;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private MessageResponse convertToMessageResponse(Message message){
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(message.getId());
        messageResponse.setSenderId(message.getSender().getId());
        messageResponse.setSenderName(message.getReceiver().getUsername());
        messageResponse.setSenderAvatar(message.getSender().getAvatar());
        messageResponse.setReceiverId(message.getReceiver().getId());
        messageResponse.setReceiverName(message.getReceiver().getUsername());
        messageResponse.setContent(message.getContent());
        messageResponse.setReceiverAvatar(message.getReceiver().getAvatar());
        messageResponse.setIsRead(message.getIsRead());
        messageResponse.setCreatedAt(message.getCreatedAt());
        return messageResponse;
    }

    private Page<MessageResponse> mapToMessageResponse(Page<Message> messageResponsePage){
        return messageResponsePage.map(this::convertToMessageResponse);
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, User sender){
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(()
                -> new ResourceNotFoundException("User  not found"));
        if(Objects.equals(sender.getId() , receiver.getId())){
            throw new BadRequestException("Cannot message yourself");
        }
        Message  message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        Message savedMessage=  messageRepository.save(message);
        return convertToMessageResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversations(User currentUser, Pageable pageable) {
        try {
            System.out.println("=== GET CONVERSATIONS ===");
            System.out.println("Current user: " + currentUser.getUsername());

            // Get all user IDs I've chatted with (both directions)
            List<Long> receiverIds = messageRepository.findReceiverIdsBySender(currentUser);
            List<Long> senderIds = messageRepository.findSenderIdsByReceiver(currentUser);

            // Combine and remove duplicates
            Set<Long> allUserIds = new HashSet<>();
            allUserIds.addAll(receiverIds);
            allUserIds.addAll(senderIds);
            allUserIds.remove(currentUser.getId()); // Remove self

            System.out.println("Found user IDs: " + allUserIds.size());

            // List to store conversation responses
            List<ConversationResponse> conversations = new ArrayList<>();

            // Loop through each user ID
            for (Long userId : allUserIds) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) continue;

                System.out.println("Processing user: " + user.getUsername());

                // Get last message
                Page<Message> messagePage = messageRepository.findBySenderOrReceiverOrderByCreatedAtDesc(
                        user, currentUser, PageRequest.of(0, 1));

                Message lastMessage = messagePage.getContent().isEmpty() ? null : messagePage.getContent().get(0);

                // Get unread count
                Long unreadCount = messageRepository.countBySenderAndReceiverAndIsReadFalse(user, currentUser);

                // Build response
                ConversationResponse response = new ConversationResponse();
                response.setUserId(user.getId());
                response.setUserName(user.getFirstName() + " " + user.getLastName());
                response.setUserAvatar(user.getAvatar());
                response.setLastMessage(lastMessage != null ? lastMessage.getContent() : "");
                response.setLastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null);
                response.setUnreadCount(unreadCount.intValue());

                // Add to list
                conversations.add(response);
            }

            System.out.println("Total conversations: " + conversations.size());

            // Sort by lastMessageTime descending (newest first)
            conversations.sort((a, b) -> {
                if (a.getLastMessageTime() == null) return 1;
                if (b.getLastMessageTime() == null) return -1;
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            });

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), conversations.size());

            List<ConversationResponse> pageContent = conversations.subList(start, end);

            System.out.println("Returning page with " + pageContent.size() + " items");
            return new PageImpl<>(pageContent, pageable, conversations.size());

        } catch (Exception e) {
            System.out.println("ERROR in getConversations: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public Page<MessageResponse> getConversation(User currentUser, Long otherUserId, Pageable pageable) {
        User user  = userRepository.findById(otherUserId).orElseThrow(()
                -> new ResourceNotFoundException("User not found"));
        Page<Message> messages = messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtAsc(
                currentUser, user,
                currentUser , user,
                pageable
        );
         messageRepository.markAsRead(user.getId(), currentUser.getId());
         return messages.map(this::convertToMessageResponse);
    }
}
