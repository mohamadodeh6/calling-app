package com.mohamadodeh6.callingapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mohamadodeh6.callingapp.databinding.ActivityLoginBinding
import com.mohamadodeh6.callingapp.ui.home.HomeActivity
import com.mohamadodeh6.callingapp.ui.register.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.usernameInput.text.toString().trim(),
                binding.passwordInput.text.toString(),
            )
        }

        binding.registerNavButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.loginButton.isEnabled = !state.loading
                state.error?.let { Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show() }
                if (state.success) {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }
            }
        }
    }
}
