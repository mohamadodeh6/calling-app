package com.mohamadodeh6.callingapp.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mohamadodeh6.callingapp.databinding.ActivityRegisterBinding
import com.mohamadodeh6.callingapp.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            viewModel.register(
                binding.usernameInput.text.toString().trim(),
                binding.passwordInput.text.toString(),
            )
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.registerButton.isEnabled = !state.loading
                state.error?.let { Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show() }
                if (state.success) {
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }
}
