package com.example.springbackend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.springbackend.entity.Chat;
import com.example.springbackend.entity.ChatUser;
import com.example.springbackend.repository.ChatRepository;
import com.example.springbackend.repository.ChatUserRepository;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatService(
            ChatRepository chatRepository,
            ChatUserRepository chatUserRepository
    ) {
        this.chatRepository = chatRepository;
        this.chatUserRepository = chatUserRepository;
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
}