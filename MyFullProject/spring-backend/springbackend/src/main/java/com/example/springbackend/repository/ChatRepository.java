package com.example.springbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.springbackend.entity.Chat;

@Repository
public interface ChatRepository
        extends JpaRepository<Chat, Long> {

                @Query("""
        SELECT c
        FROM Chat c
        JOIN ChatUser cu
            ON c.chatId = cu.chatId
        WHERE cu.userId = :userId
    """)
    List<Chat> findChatsByUserId(Long userId);
}
