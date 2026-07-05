package com.example.myapplication.session

object SessionManager {

    var currentUserId: Long? = null

    fun isLoggedIn(): Boolean {
        return currentUserId != null
    }

    fun logout() {
        currentUserId = null
    }
}