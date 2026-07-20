package com.mohamadodeh6.callingapp.data.websocket

import com.google.gson.Gson
import com.mohamadodeh6.callingapp.BuildConfig
import com.mohamadodeh6.callingapp.models.SignalMessage
import com.mohamadodeh6.callingapp.utils.SessionStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionStore: SessionStore,
) {
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _events = MutableSharedFlow<SignalMessage>(extraBufferCapacity = 64)
    val events: SharedFlow<SignalMessage> = _events

    fun connect() {
        val token = sessionStore.token ?: return
        val request = Request.Builder()
            .url("${BuildConfig.WS_BASE_URL}?token=$token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { gson.fromJson(text, SignalMessage::class.java) }
                    .onSuccess { _events.tryEmit(it) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _events.tryEmit(SignalMessage(type = "error"))
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }

    fun send(signalMessage: SignalMessage) {
        webSocket?.send(gson.toJson(signalMessage))
    }
}
