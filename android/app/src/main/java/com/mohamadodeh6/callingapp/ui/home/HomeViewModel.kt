package com.mohamadodeh6.callingapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamadodeh6.callingapp.data.repository.CallRepository
import com.mohamadodeh6.callingapp.data.repository.UserRepository
import com.mohamadodeh6.callingapp.models.SignalMessage
import com.mohamadodeh6.callingapp.models.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val callRepository: CallRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        callRepository.connectSignal()
        observeSignals()
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            runCatching { userRepository.getUsers() }
                .onSuccess { users -> _state.value = _state.value.copy(users = users) }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Failed loading users") }
        }
    }

    fun callUser(username: String) {
        callRepository.sendCallRequest(username)
        _state.value = _state.value.copy(outgoingTo = username)
    }

    private fun observeSignals() {
        viewModelScope.launch {
            callRepository.signalEvents.collect { signal ->
                when (signal.type) {
                    "call_request" -> _state.value = _state.value.copy(incomingFrom = signal.from)
                    "presence" -> loadUsers()
                    else -> Unit
                }
            }
        }
    }

    override fun onCleared() {
        callRepository.disconnectSignal()
        super.onCleared()
    }
}

data class HomeState(
    val users: List<UserDto> = emptyList(),
    val incomingFrom: String? = null,
    val outgoingTo: String? = null,
    val error: String? = null,
)
