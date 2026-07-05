package com.example.springbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springbackend.dto.ActivityResponse;
import com.example.springbackend.dto.CreateActivityRequest;
import com.example.springbackend.entity.Activity;
import com.example.springbackend.entity.Chat;
import com.example.springbackend.entity.User;
import com.example.springbackend.repository.ActivityRepository;
import com.example.springbackend.repository.ChatRepository;
import com.example.springbackend.repository.ChatUserRepository;
import com.example.springbackend.repository.UserRepository;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;

    private final ChatService chatService;
    

    public ActivityService(
            ActivityRepository activityRepository,
            UserRepository userRepository,
            ChatRepository chatRepository,
            ChatUserRepository chatUserRepository,
            ChatService chatService
    ) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.chatUserRepository = chatUserRepository;
        this.chatService = chatService;
    }

    public Activity create(CreateActivityRequest dto) {


        Chat chat = chatService.createChat(
            dto.getTitle(),
            true,
            dto.getCreatorId()
        );

        Activity activity = new Activity();


        System.out.println(
        "CREATED CHAT ID = "
        + chat.getChatId()
);

        activity.setChatId(chat.getChatId());

        activity.setCreatorId(dto.getCreatorId());
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setLatitude(dto.getLatitude());
        activity.setLongitude(dto.getLongitude());
        activity.setEventTime(dto.getEventTime());
        activity.setIsCancelled(false);

        User creator =
                userRepository.findById(dto.getCreatorId())
                        .orElseThrow();

        activity.getParticipants().add(creator);

        return activityRepository.save(activity);
    }


    public void deleteActivity(Long activityId, Long userId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow();

        if (!activity.getCreatorId().equals(userId)) {
            throw new RuntimeException("Only creator can delete activity");
        }

        activityRepository.delete(activity);
    }

    public Activity joinActivity(Long activityId, Long userId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // prevent duplicate joins
        if (activity.getParticipants().contains(user)) {
            return activity;
        }

        activity.getParticipants().add(user);

        return activityRepository.save(activity);
    }

    public List<ActivityResponse> getAllActivities() {

        List<Activity> activities = activityRepository.findAll();

        return activities.stream().map(activity -> {

            User creator = userRepository.findById(activity.getCreatorId())
                    .orElse(null);

            ActivityResponse dto = new ActivityResponse();

            dto.setActivityId(activity.getActivityId());
            dto.setCreatorId(activity.getCreatorId());
            dto.setCreatorName(
                    creator != null ? creator.getName() : "Unknown"
            );

            dto.setChatId(activity.getChatId());
            dto.setTitle(activity.getTitle());
            dto.setDescription(activity.getDescription());
            dto.setLatitude(activity.getLatitude());
            dto.setLongitude(activity.getLongitude());
            dto.setEventTime(activity.getEventTime());
            dto.setIsCancelled(activity.getIsCancelled());

            return dto;

        }).toList();
    }



    public Activity leaveActivity(Long activityId, Long userId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        activity.getParticipants().remove(user);

        return activityRepository.save(activity);
    }

}
