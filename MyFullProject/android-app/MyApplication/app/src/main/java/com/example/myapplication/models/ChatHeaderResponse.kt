package com.example.myapplication.models

data class ChatHeaderResponse(
    val activityTitle: String,
    val participantCount: Int,
    val participants: List<Participant>
)