package com.mohamadodeh6.callingapp.data.repository

import com.mohamadodeh6.callingapp.data.api.AuthApi
import com.mohamadodeh6.callingapp.models.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: AuthApi,
) {
    suspend fun getUsers(): List<UserDto> = api.getUsers().users
}
