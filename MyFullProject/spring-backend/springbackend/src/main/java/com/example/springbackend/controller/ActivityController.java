package com.example.springbackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbackend.dto.ActivityResponse;
import com.example.springbackend.dto.CreateActivityRequest;
import com.example.springbackend.entity.Activity;
import com.example.springbackend.service.ActivityService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(
            ActivityService activityService
    ) {
        this.activityService = activityService;
    }

    @PostMapping
    public Activity create(
            @RequestBody CreateActivityRequest request
    ) {
        return activityService.create(request);
    }

    @PostMapping("/{activityId}/join/{userId}")
    public Activity join(
            @PathVariable Long activityId,
            @PathVariable Long userId
    ) {

        return activityService.joinActivity(
                activityId,
                userId
        );
    }

    @GetMapping
    public List<ActivityResponse> getAllActivities() {
        return activityService.getAllActivities();
    }

    @DeleteMapping("/{activityId}/{userId}")
    public void delete(
            @PathVariable Long activityId,
            @PathVariable Long userId
    ) {

        activityService.deleteActivity(
                activityId,
                userId
        );
    }



    @PostMapping("/{activityId}/leave/{userId}")
    public Activity leaveActivity(
            @PathVariable Long activityId,
            @PathVariable Long userId
    ) {
        return activityService.leaveActivity(activityId, userId);
    }
    

    
}
