package com.example.springbackend.controller;

import com.example.springbackend.dto.LoginRequest;
import com.example.springbackend.dto.RegisterRequest;
import com.example.springbackend.entity.User;
import com.example.springbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}