package com.mohamadodeh6.callingapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamadodeh6.callingapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { authRepository.register(username, password) }
                .onSuccess { _state.value = RegisterState(success = true) }
                .onFailure { _state.value = RegisterState(error = it.message ?: "Register failed") }
        }
    }
}

data class RegisterState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)
