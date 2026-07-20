package com.mohamadodeh6.callingapp.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription?) = Unit
    override fun onSetSuccess() = Unit
    override fun onCreateFailure(error: String?) = Unit
    override fun onSetFailure(error: String?) = Unit
}
