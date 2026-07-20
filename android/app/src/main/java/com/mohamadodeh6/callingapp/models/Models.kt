package com.mohamadodeh6.callingapp.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val username: String, val password: String)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val online: Boolean = false,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UsersResponse(val users: List<UserDto>)

@Serializable
data class SignalMessage(
    val type: String,
    val to: String? = null,
    val from: String? = null,
    val sdp: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
)
