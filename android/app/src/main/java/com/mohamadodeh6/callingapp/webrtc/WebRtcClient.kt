package com.mohamadodeh6.callingapp.webrtc

import android.content.Context
import com.mohamadodeh6.callingapp.BuildConfig
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcClient @Inject constructor(
    context: Context,
) {
    interface Listener {
        fun onIceCandidate(candidate: IceCandidate)
        fun onConnected()
    }

    private val factory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var listener: Listener? = null

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions(),
        )
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun createPeerConnection(listener: Listener) {
        this.listener = listener
        val iceServers = mutableListOf(
            PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER).createIceServer(),
        )
        if (BuildConfig.TURN_URL.isNotEmpty()) {
            iceServers += PeerConnection.IceServer.builder(BuildConfig.TURN_URL)
                .setUsername(BuildConfig.TURN_USERNAME)
                .setPassword(BuildConfig.TURN_CREDENTIAL)
                .createIceServer()
        }

        peerConnection = factory.createPeerConnection(
            iceServers,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    this@WebRtcClient.listener?.onIceCandidate(candidate)
                }

                override fun onSignalingChange(newState: PeerConnection.SignalingState) = Unit
                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                    if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                        this@WebRtcClient.listener?.onConnected()
                    }
                }
                override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) = Unit
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
                override fun onAddStream(stream: org.webrtc.MediaStream) = Unit
                override fun onRemoveStream(stream: org.webrtc.MediaStream) = Unit
                override fun onDataChannel(dataChannel: org.webrtc.DataChannel) = Unit
                override fun onRenegotiationNeeded() = Unit
                override fun onAddTrack(receiver: org.webrtc.RtpReceiver, streams: Array<out org.webrtc.MediaStream>) = Unit
            },
        )

        audioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("audio_track", audioSource)
        val stream = factory.createLocalMediaStream("local_stream")
        stream.addTrack(localAudioTrack)
        peerConnection?.addStream(stream)
    }

    fun createOffer(onCreated: (SessionDescription) -> Unit) {
        peerConnection?.createOffer(object : org.webrtc.SdpObserverAdapter() {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                sessionDescription ?: return
                setLocalDescription(sessionDescription)
                onCreated(sessionDescription)
            }
        }, MediaConstraints())
    }

    fun createAnswer(onCreated: (SessionDescription) -> Unit) {
        peerConnection?.createAnswer(object : org.webrtc.SdpObserverAdapter() {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                sessionDescription ?: return
                setLocalDescription(sessionDescription)
                onCreated(sessionDescription)
            }
        }, MediaConstraints())
    }

    fun setRemoteDescription(type: SessionDescription.Type, sdp: String) {
        peerConnection?.setRemoteDescription(
            org.webrtc.SdpObserverAdapter(),
            SessionDescription(type, sdp),
        )
    }

    private fun setLocalDescription(sdp: SessionDescription) {
        peerConnection?.setLocalDescription(org.webrtc.SdpObserverAdapter(), sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun setMute(muted: Boolean) {
        localAudioTrack?.setEnabled(!muted)
    }

    fun close() {
        localAudioTrack?.dispose()
        audioSource?.dispose()
        peerConnection?.close()
        peerConnection?.dispose()
        localAudioTrack = null
        audioSource = null
        peerConnection = null
    }
}
