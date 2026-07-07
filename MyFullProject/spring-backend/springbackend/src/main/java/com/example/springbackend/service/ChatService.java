package com.example.springbackend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springbackend.dto.ChatHeaderResponse;
import com.example.springbackend.dto.MessageDto;
import com.example.springbackend.dto.ParticipantDto;
import com.example.springbackend.dto.SendMessageRequest;
import com.example.springbackend.entity.Activity;
import com.example.springbackend.entity.Chat;
import com.example.springbackend.entity.ChatUser;
import com.example.springbackend.entity.Message;
import com.example.springbackend.entity.MessageSeen;
import com.example.springbackend.entity.User;
import com.example.springbackend.repository.ActivityRepository;
import com.example.springbackend.repository.ChatRepository;
import com.example.springbackend.repository.ChatUserRepository;
import com.example.springbackend.repository.MessageRepository;
import com.example.springbackend.repository.UserRepository;
import com.example.springbackend.repository.MessageSeenRepository;;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageSeenRepository messageSeenRepository;

    public ChatService(
            ChatRepository chatRepository,
            ChatUserRepository chatUserRepository,
            ActivityRepository activityRepository,
            UserRepository userRepository,
            MessageRepository messageRepository,
            MessageSeenRepository messageSeenRepository
    ) {
        this.chatRepository = chatRepository;
        this.chatUserRepository = chatUserRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageSeenRepository = messageSeenRepository;
    }

    public Chat createChat(
            String name,
            boolean isGroup,
            Long creatorId
    ) {

        Chat chat = new Chat();

        chat.setName(name);
        chat.setIsGroup(isGroup);

        chat = chatRepository.save(chat);

        System.out.println("Chat saved: " + chat.getChatId());

        ChatUser member = new ChatUser();

        member.setChatId(chat.getChatId());
        member.setUserId(creatorId);
        member.setJoinedAt(LocalDateTime.now());

        System.out.println("Saving ChatUser...");
        chatUserRepository.save(member);
        System.out.println("ChatUser saved.");

        return chat;
    }



    public void addUserToChat(Long chatId, Long userId) {


        System.out.println("Adding user " + userId + " to chat " + chatId);

        if (chatUserRepository.existsByChatIdAndUserId(chatId, userId)) {
            return;
        }

        ChatUser member = new ChatUser();

        member.setChatId(chatId);
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());

        chatUserRepository.save(member);

        System.out.println("User added to chat.");
    }

    public List<Chat> getUserChats(Long userId) {

        return chatRepository.findChatsByUserId(userId);
    }


    public ChatHeaderResponse getChatHeader(Long chatId) {

        Activity activity = activityRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        List<ChatUser> chatUsers =
                chatUserRepository.findByChatId(chatId);

        List<ParticipantDto> participants = new ArrayList<>();

        for (ChatUser chatUser : chatUsers) {

            User user = userRepository
                    .findById(chatUser.getUserId())
                    .orElseThrow();

            ParticipantDto dto = new ParticipantDto();

            
            
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setProfileImageId(user.getProfileImageId());

            participants.add(dto);
        }

        ChatHeaderResponse response =
                new ChatHeaderResponse();

        response.setActivityId(activity.getActivityId());
        response.setActivityTitle(activity.getTitle());
        response.setParticipantCount(participants.size());
        response.setParticipants(participants);

        return response;
    }


    public List<MessageDto> getMessages(Long chatId) {

        List<Message> messages =
                messageRepository.findByChatIdOrderBySentAtAsc(chatId);

        List<MessageDto> result = new ArrayList<>();

        for (Message message : messages) {

            User sender = userRepository
                    .findById(message.getSenderId())
                    .orElseThrow();

            MessageDto dto = new MessageDto();

            dto.setId(message.getMessageId());
            dto.setSenderId(sender.getId());
            dto.setSenderName(sender.getName());
            dto.setProfileImageId(sender.getProfileImageId());
            dto.setStatus(message.getStatus());


            dto.setContent(message.getContent());
            dto.setSentAt(message.getSentAt());


            dto.setReplyToMessageId(message.getReplyToMessageId());

            if (message.getReplyToMessageId() != null) {

                Message repliedMessage = messageRepository
                        .findById(message.getReplyToMessageId())
                        .orElse(null);

                if (repliedMessage != null) {

                    User repliedSender = userRepository
                            .findById(repliedMessage.getSenderId())
                            .orElse(null);

                    dto.setReplyPreview(repliedMessage.getContent());

                    if (repliedSender != null) {
                        dto.setReplySenderName(repliedSender.getName());
                    }
                }
            }



            result.add(dto);
        }

        return result;
    }


    public MessageDto sendMessage(
            Long chatId,
            SendMessageRequest request
    ) {

        User sender = userRepository
                .findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();

        System.out.println("replyToMessageId = " + request.getReplyToMessageId());
        System.out.println("replyPreview = " + request.getReplyPreview());
        System.out.println("replySenderName = " + request.getReplySenderName());

        message.setChatId(chatId);
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());

        if(request.getReplyToMessageId() != null){
            message.setReplyToMessageId(request.getReplyToMessageId());
            message.setReplyPreview(request.getReplyPreview());
            message.setReplySenderName(request.getReplySenderName());
        }

        message.setStatus("sent");

        message.setSentAt(LocalDateTime.now());

        message = messageRepository.save(message);

        MessageDto dto = new MessageDto();

        dto.setId(message.getMessageId());
        dto.setSenderId(sender.getId());
        dto.setSenderName(sender.getName());
        dto.setProfileImageId(sender.getProfileImageId());

        dto.setReplyToMessageId(message.getReplyToMessageId());
        dto.setReplyPreview(message.getReplyPreview());
        dto.setReplySenderName(message.getReplySenderName());

        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setSentAt(message.getSentAt());

        return dto;
    }


    public void markMessagesAsSeen(Long chatId, Long viewerId) {

        List<Message> messages =
                messageRepository.findByChatIdOrderBySentAtAsc(chatId);

        Activity activity =
                activityRepository.findByChatId(chatId)
                        .orElseThrow();

        int participants =
                activity.getParticipants().size();

        for (Message message : messages) {

            if (message.getSenderId().equals(viewerId))
                continue;

            if (!messageSeenRepository.existsByMessageIdAndUserId(
                    message.getMessageId(),
                    viewerId
            )) {

                MessageSeen seen = new MessageSeen();

                seen.setMessageId(message.getMessageId());
                seen.setUserId(viewerId);
                seen.setSeenAt(LocalDateTime.now());

                messageSeenRepository.save(seen);
            }

            long seenCount =
                    messageSeenRepository.countByMessageId(
                            message.getMessageId());

            // Everyone except the sender has seen it
            if (seenCount >= participants - 1
                    && !"seen".equals(message.getStatus())) {

                message.setStatus("seen");

                messageRepository.save(message);
            }
        }
    }


}