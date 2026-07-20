package com.mohamadodeh6.callingapp.data.repository

import com.mohamadodeh6.callingapp.data.websocket.SignalSocketClient
import com.mohamadodeh6.callingapp.models.SignalMessage
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    private val signalSocketClient: SignalSocketClient,
) {
    val signalEvents: SharedFlow<SignalMessage> = signalSocketClient.events

    fun connectSignal() = signalSocketClient.connect()

    fun disconnectSignal() = signalSocketClient.disconnect()

    fun sendCallRequest(to: String) = signalSocketClient.send(SignalMessage(type = "call_request", to = to))

    fun sendOffer(to: String, sdp: String) = signalSocketClient.send(SignalMessage(type = "offer", to = to, sdp = sdp))

    fun sendAnswer(to: String, sdp: String) = signalSocketClient.send(SignalMessage(type = "answer", to = to, sdp = sdp))

    fun sendIce(to: String, candidate: String, sdpMid: String?, sdpMLineIndex: Int?) = signalSocketClient.send(
        SignalMessage(
            type = "ice",
            to = to,
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex,
        ),
    )

    fun hangup(to: String?) = signalSocketClient.send(SignalMessage(type = "hangup", to = to))
}
