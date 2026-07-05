package com.example.springbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springbackend.entity.ChatUser;

@Repository
public interface ChatUserRepository
        extends JpaRepository<ChatUser, Long> {
}
