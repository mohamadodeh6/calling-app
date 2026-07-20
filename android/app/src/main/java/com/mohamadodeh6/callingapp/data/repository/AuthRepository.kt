package com.mohamadodeh6.callingapp.data.repository

import com.mohamadodeh6.callingapp.data.api.AuthApi
import com.mohamadodeh6.callingapp.models.AuthRequest
import com.mohamadodeh6.callingapp.models.UserDto
import com.mohamadodeh6.callingapp.utils.SessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
) {
    suspend fun login(username: String, password: String): UserDto {
        val response = api.login(AuthRequest(username, password))
        sessionStore.token = response.token
        sessionStore.username = response.user.username
        return response.user
    }

    suspend fun register(username: String, password: String): UserDto {
        val response = api.register(AuthRequest(username, password))
        sessionStore.token = response.token
        sessionStore.username = response.user.username
        return response.user
    }

    suspend fun logout() {
        runCatching { api.logout() }
        sessionStore.clear()
    }
}
