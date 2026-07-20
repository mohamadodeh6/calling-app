package com.mohamadodeh6.callingapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohamadodeh6.callingapp.databinding.ActivityHomeBinding
import com.mohamadodeh6.callingapp.ui.call.ActiveCallActivity
import com.mohamadodeh6.callingapp.ui.call.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = UserAdapter { user ->
        if (hasAudioPermission()) {
            viewModel.callUser(user.username)
            startActivity(ActiveCallActivity.createOutgoingIntent(this, user.username))
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Record audio permission is required for calls", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usersRecycler.layoutManager = LinearLayoutManager(this)
        binding.usersRecycler.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadUsers() }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.submit(state.users)
                binding.swipeRefresh.isRefreshing = false
                state.error?.let { Toast.makeText(this@HomeActivity, it, Toast.LENGTH_SHORT).show() }
                state.incomingFrom?.let {
                    startActivity(IncomingCallActivity.createIntent(this@HomeActivity, it))
                }
            }
        }
    }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}
