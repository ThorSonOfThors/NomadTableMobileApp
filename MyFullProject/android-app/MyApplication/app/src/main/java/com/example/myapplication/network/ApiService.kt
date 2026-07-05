package com.example.myapplication.network

import com.example.myapplication.models.Activity
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.RegisterRequest
import com.example.myapplication.models.CreateActivityRequest
import com.example.myapplication.models.User
import com.example.myapplication.models.UserImage
import okhttp3.MultipartBody

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<User>

    @POST("api/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<User>

    @GET("api/users/{id}")
    fun getUser(
        @Path("id") id: Long
    ): Call<User>

    @PUT("api/users/{id}")
    fun updateUser(
        @Path("id") id: Long,
        @Body user: User
    ): Call<User>


    @Multipart
    @POST("api/users/{id}/images")
    fun uploadImage(
        @Path("id") userId: Long,

        @Part file: MultipartBody.Part
    ): Call<UserImage>


    @GET("api/users/{id}/images")
    fun getImages(
        @Path("id") userId: Long
    ): Call<List<UserImage>>

    @DELETE("api/users/{userId}/images/{imageId}")
    fun deleteImage(
        @Path("userId") userId: Long,
        @Path("imageId") imageId: Long
    ): Call<Void>


    @GET("api/activities")
    fun getActivities(): Call<List<Activity>>

    @POST("api/activities")
    fun createActivity(
        @Body request: CreateActivityRequest
    ): Call<Activity>

}