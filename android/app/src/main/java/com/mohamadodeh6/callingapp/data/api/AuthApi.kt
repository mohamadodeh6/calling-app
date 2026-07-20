package com.mohamadodeh6.callingapp.data.api

import com.mohamadodeh6.callingapp.models.AuthRequest
import com.mohamadodeh6.callingapp.models.AuthResponse
import com.mohamadodeh6.callingapp.models.UsersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/logout")
    suspend fun logout()

    @GET("/users")
    suspend fun getUsers(): UsersResponse
}
