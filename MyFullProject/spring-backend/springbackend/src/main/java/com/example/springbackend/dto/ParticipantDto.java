package com.example.springbackend.dto;


public class ParticipantDto {

    private Long id;
    private String name;
    private Long profileImageId;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getProfileImageId() {
        return profileImageId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImageId(Long profileImageId) {
        this.profileImageId = profileImageId;
    }
}
