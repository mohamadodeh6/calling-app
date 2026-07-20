package com.mohamadodeh6.callingapp.ui.call

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mohamadodeh6.callingapp.databinding.ActivityActiveCallBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActiveCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActiveCallBinding
    private val viewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val peer = intent.getStringExtra(EXTRA_PEER).orEmpty()
        binding.peerName.text = peer

        if (intent.getBooleanExtra(EXTRA_IS_OUTGOING, false)) {
            viewModel.startOutgoingCall(peer)
        }

        binding.muteButton.setOnClickListener { viewModel.toggleMute() }
        binding.speakerButton.setOnClickListener {
            viewModel.toggleSpeaker()
            setSpeakerEnabled(viewModel.state.value.speakerEnabled)
        }
        binding.hangupButton.setOnClickListener {
            viewModel.hangup()
            finish()
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.status.text = state.phase.name
                binding.duration.text = formatDuration(state.durationSec)
                if (state.phase == CallPhase.ENDED) finish()
            }
        }
    }

    private fun setSpeakerEnabled(enabled: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = enabled
    }

    private fun formatDuration(totalSeconds: Long): String {
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    companion object {
        private const val EXTRA_PEER = "extra_peer"
        private const val EXTRA_IS_OUTGOING = "extra_outgoing"

        fun createOutgoingIntent(context: Context, peer: String): Intent =
            Intent(context, ActiveCallActivity::class.java)
                .putExtra(EXTRA_PEER, peer)
                .putExtra(EXTRA_IS_OUTGOING, true)

        fun createIncomingIntent(context: Context, peer: String): Intent =
            Intent(context, ActiveCallActivity::class.java)
                .putExtra(EXTRA_PEER, peer)
                .putExtra(EXTRA_IS_OUTGOING, false)
    }
}
