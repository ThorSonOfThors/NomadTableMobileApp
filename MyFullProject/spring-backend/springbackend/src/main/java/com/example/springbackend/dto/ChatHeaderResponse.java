package com.example.springbackend.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class ChatHeaderResponse {

    private Long activityId;

    private String activityTitle;

    private Integer participantCount;

    private List<ParticipantDto> participants;

    // getters/setters
}
