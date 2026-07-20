package com.mohamadodeh6.callingapp.ui.call

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mohamadodeh6.callingapp.databinding.ActivityIncomingCallBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomingCallBinding
    private val viewModel: CallViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) answer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val from = intent.getStringExtra(EXTRA_CALLER).orEmpty()
        binding.callerName.text = from

        binding.acceptButton.setOnClickListener {
            if (hasAudioPermission()) answer() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        binding.rejectButton.setOnClickListener {
            viewModel.rejectCall(from)
            finish()
        }
    }

    private fun answer() {
        val from = intent.getStringExtra(EXTRA_CALLER).orEmpty()
        viewModel.answerIncomingCall(from)
        startActivity(ActiveCallActivity.createIncomingIntent(this, from))
        finish()
    }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val EXTRA_CALLER = "extra_caller"

        fun createIntent(context: Context, caller: String): Intent =
            Intent(context, IncomingCallActivity::class.java).putExtra(EXTRA_CALLER, caller)
    }
}
