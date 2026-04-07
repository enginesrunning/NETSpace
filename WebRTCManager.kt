package com.example.netspace.webrtc

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.*

class WebRTCManager(
    private val context: Context,
    private val signalingClient: SignalingClient
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var rootEglBase: EglBase? = null

    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoCapturer: VideoCapturer? = null

    private var targetId: String? = null

    // For binding streams to Compose AndroidView
    var localVideoSink: VideoSink? = null
        set(value) {
            field = value
            value?.let { localVideoTrack?.addSink(it) }
        }

    var remoteVideoSink: VideoSink? = null

    private val _onCallEnded = MutableSharedFlow<Unit>()
    val onCallEnded: SharedFlow<Unit> = _onCallEnded

    init {
        initWebRTC()
    }

    private fun initWebRTC() {
        if (rootEglBase != null) return

        rootEglBase = EglBase.create()
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase?.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase?.eglBaseContext, true, true))
            .createPeerConnectionFactory()
    }

    fun getEglBaseContext(): EglBase.Context? = rootEglBase?.eglBaseContext

    fun startLocalMedia() {
        val factory = peerConnectionFactory ?: return

        // Audio
        val audioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource)
        localAudioTrack?.setEnabled(true)

        // Video
        videoCapturer = createCameraCapturer(context)
        videoCapturer?.let { capturer ->
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase?.eglBaseContext)
            val videoSource = factory.createVideoSource(capturer.isScreencast)
            capturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            capturer.startCapture(1280, 720, 30)

            localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource)
            localVideoTrack?.setEnabled(true)
        }
    }

    private fun createCameraCapturer(context: Context): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        // Try front facing first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        // Fallback to back facing
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    fun initiateCall(targetId: String) {
        this.targetId = targetId
        createPeerConnection()
        
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let { sdp ->
                    peerConnection?.setLocalDescription(SdpObserverAdapter(), sdp)
                    signalingClient.sendOffer(targetId, sdp.description)
                }
            }
        }, constraints)
    }

    fun receiveOffer(senderId: String, sdpStr: String) {
        this.targetId = senderId
        createPeerConnection()

        val sdp = SessionDescription(SessionDescription.Type.OFFER, sdpStr)
        peerConnection?.setRemoteDescription(object : SdpObserverAdapter() {
            override fun onSetSuccess() {
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                }
                peerConnection?.createAnswer(object : SdpObserverAdapter() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let { answer ->
                            peerConnection?.setLocalDescription(SdpObserverAdapter(), answer)
                            signalingClient.sendAnswer(senderId, answer.description)
                        }
                    }
                }, constraints)
            }
        }, sdp)
    }

    fun receiveAnswer(sdpStr: String) {
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpStr)
        peerConnection?.setRemoteDescription(SdpObserverAdapter(), sdp)
    }

    fun receiveIceCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String) {
        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        peerConnection?.addIceCandidate(candidate)
    }

    private fun createPeerConnection() {
        if (peerConnection != null) return

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnectionObserverAdapter() {
              override fun onIceCandidate(iceCandidate: IceCandidate?) {
                iceCandidate?.let { candidate ->
                    targetId?.let { id ->
                        signalingClient.sendIceCandidate(
                            id,
                            candidate.sdpMid,
                            candidate.sdpMLineIndex,
                            candidate.sdp
                        )
                    }
                }
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                super.onAddStream(mediaStream)
                mediaStream?.videoTracks?.firstOrNull()?.let { remoteTrack ->
                    remoteVideoSink?.let { sink ->
                        remoteTrack.addSink(sink)
                    }
                }
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                super.onAddTrack(receiver, mediaStreams)
                receiver?.track()?.takeIf { it.kind() == "video" }?.let { track ->
                    remoteVideoSink?.let { sink ->
                        (track as? VideoTrack)?.addSink(sink)
                    }
                }
            }
        })

        // Add local tracks
        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("ARDAMS")) }
        localVideoTrack?.let { peerConnection?.addTrack(it, listOf("ARDAMS")) }
    }

    fun setMute(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun setVideoEnabled(isEnabled: Boolean) {
        localVideoTrack?.setEnabled(isEnabled)
    }

    fun endCall() {
        targetId?.let { signalingClient.endCall(it) }
        dispose()
    }

    fun dispose() {
        try {
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            surfaceTextureHelper?.dispose()
            peerConnection?.close()
            peerConnection?.dispose()
            
            localVideoTrack = null
            localAudioTrack = null
            videoCapturer = null
            peerConnection = null
            targetId = null
        } catch (e: Exception) {
            Log.e("WebRTCManager", "Error disposing WebRTC", e)
        }
    }

    // Helper Adapters
    open class SdpObserverAdapter : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }

    open class PeerConnectionObserverAdapter : PeerConnection.Observer {
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidate(p0: IceCandidate?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onAddStream(p0: MediaStream?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onDataChannel(p0: DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
    }
}
