package com.mohamadodeh6.callingapp.ui.call

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamadodeh6.callingapp.data.repository.CallRepository
import com.mohamadodeh6.callingapp.models.SignalMessage
import com.mohamadodeh6.callingapp.webrtc.WebRtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val webRtcClient: WebRtcClient,
) : ViewModel(), WebRtcClient.Listener {
    private val _state = MutableStateFlow(CallState())
    val state: StateFlow<CallState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        callRepository.connectSignal()
        webRtcClient.createPeerConnection(this)
        observeSignalEvents()
    }

    fun startOutgoingCall(to: String) {
        _state.value = _state.value.copy(peerUsername = to, phase = CallPhase.OUTGOING)
        callRepository.sendCallRequest(to)
        webRtcClient.createOffer {
            callRepository.sendOffer(to, it.description)
        }
    }

    fun answerIncomingCall(from: String) {
        _state.value = _state.value.copy(peerUsername = from, phase = CallPhase.CONNECTING)
        webRtcClient.createAnswer {
            callRepository.sendAnswer(from, it.description)
        }
    }

    fun rejectCall(from: String) {
        callRepository.hangup(from)
        _state.value = CallState(phase = CallPhase.IDLE)
    }

    fun toggleMute() {
        val newMute = !_state.value.muted
        webRtcClient.setMute(newMute)
        _state.value = _state.value.copy(muted = newMute)
    }

    fun toggleSpeaker() {
        _state.value = _state.value.copy(speakerEnabled = !_state.value.speakerEnabled)
    }

    fun hangup() {
        callRepository.hangup(_state.value.peerUsername)
        webRtcClient.close()
        timerJob?.cancel()
        _state.value = CallState(phase = CallPhase.ENDED)
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        val peer = _state.value.peerUsername ?: return
        callRepository.sendIce(peer, candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
    }

    override fun onConnected() {
        _state.value = _state.value.copy(phase = CallPhase.ACTIVE)
        val start = SystemClock.elapsedRealtime()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val seconds = (SystemClock.elapsedRealtime() - start) / 1000
                _state.value = _state.value.copy(durationSec = seconds)
            }
        }
    }

    private fun observeSignalEvents() {
        viewModelScope.launch {
            callRepository.signalEvents.collect { signal ->
                when (signal.type) {
                    "offer" -> {
                        val from = signal.from ?: return@collect
                        val sdp = signal.sdp ?: return@collect
                        _state.value = _state.value.copy(peerUsername = from, phase = CallPhase.INCOMING)
                        webRtcClient.setRemoteDescription(SessionDescription.Type.OFFER, sdp)
                    }
                    "answer" -> {
                        val sdp = signal.sdp ?: return@collect
                        webRtcClient.setRemoteDescription(SessionDescription.Type.ANSWER, sdp)
                    }
                    "ice" -> {
                        val candidate = signal.candidate ?: return@collect
                        val peerCandidate = IceCandidate(signal.sdpMid, signal.sdpMLineIndex ?: 0, candidate)
                        webRtcClient.addIceCandidate(peerCandidate)
                    }
                    "hangup" -> {
                        timerJob?.cancel()
                        _state.value = _state.value.copy(phase = CallPhase.ENDED)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        webRtcClient.close()
        callRepository.disconnectSignal()
        super.onCleared()
    }
}

data class CallState(
    val peerUsername: String? = null,
    val phase: CallPhase = CallPhase.IDLE,
    val muted: Boolean = false,
    val speakerEnabled: Boolean = false,
    val durationSec: Long = 0,
)

enum class CallPhase {
    IDLE,
    OUTGOING,
    INCOMING,
    CONNECTING,
    ACTIVE,
    ENDED,
}
