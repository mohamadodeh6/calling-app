package com.mohamadodeh6.callingapp.ui.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { authRepository.login(username, password) }
                .onSuccess { _state.value = LoginState(success = true) }
                .onFailure { _state.value = LoginState(error = it.message ?: "Login failed") }
        }
    }
}

data class LoginState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)
