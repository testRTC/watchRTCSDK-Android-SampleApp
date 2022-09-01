package com.spearline.webrtc

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.spearline.utils.WatchRTCUtils
import org.webrtc.*


class RTCClient(
    context: Context,
    private val meetingID: String,
    observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    val TAG = "RTCClient"

    var remoteSessionDescription: SessionDescription? = null

    val db = Firebase.firestore

    val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapturer(context) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Context) {
        Log.d("Testing", "initPeerConnectionFactory() called.")
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        Log.d("Testing", "buildPeerConnectionFactory() called.")
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        Log.d("Testing", "buildPeerConnection() called.")
        return peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )
    }


    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        Log.d("Testing", "initSurfaceView() called.")
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        Log.d("Testing", "startLocalVideoCapture() called.")
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(320, 240, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

//        Thread.sleep(2000)

        peerConnection?.addStream(localStream)
        WatchRTCUtils.connect(meetingID)
    }

    private fun PeerConnection.call(sdpObserver: SdpObserver, meetingID: String) {
        Log.d("Testing", "call() called.")

        WatchRTCUtils.logEvents(
            "createOffer",
            Gson().toJson(constraints)
        )
        createOffer(object : SdpObserver by sdpObserver {

            override fun onCreateSuccess(desc: SessionDescription?) {
                WatchRTCUtils.logEvents("createOfferOnSuccess", Gson().toJson(desc))
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
//                        Log.d(TAG, "PeerConnection: ${peerConnection.toString()}")

                        WatchRTCUtils.logEvents(
                            "setLocalDescription",
                            offer
                        )

                        db.collection("calls").document(meetingID)
                            .set(offer)
                            .addOnSuccessListener {
                                Log.e(TAG, "DocumentSnapshot added")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding document", e)
                            }
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailure: $p0")

                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
//                WatchRTCUtils.logEvents(1, WatchRTCEvents.WATCHRTC.toString(), Gson().toJson(p0).toString())
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, meetingID: String) {
        Log.d("Testing", "answer() called.")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        WatchRTCUtils.logEvents(
            "createAnswer",
            Gson().toJson(constraints)
        )
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                val answer = hashMapOf(
                    "sdp" to desc?.description,
                    "type" to desc?.type
                )
                WatchRTCUtils.logEvents(
                    "createAnswerOnSuccess",
                    Gson().toJson(answer)
                )
                db.collection("calls").document(meetingID)
                    .set(answer)
                    .addOnSuccessListener {
                        Log.e(TAG, "DocumentSnapshot added")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding document", e)
                    }
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                        WatchRTCUtils.logEvents(
                            "setLocalDescription",
                            Gson().toJson(p0)
                        )
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailureLocal: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailureRemote: $p0")
//                WatchRTCUtils.logEvents(1, WatchRTCEvents.WATCHRTC.toString(), Gson().toJson(p0).toString())
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, meetingID: String) =
        peerConnection?.call(sdpObserver, meetingID)

    fun answer(sdpObserver: SdpObserver, meetingID: String) =
        peerConnection?.answer(sdpObserver, meetingID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        WatchRTCUtils.logEvents(
            "setRemoteDescription",
            sessionDescription
        )
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(TAG, "onCreateSuccessRemoteSession: Description $p0")
                WatchRTCUtils.logEvents(
                    "setRemoteDescriptionOnSuccess",
                    p0
                )
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(meetingID: String) {
        db.collection("calls").document(meetingID).collection("candidates")
            .get().addOnSuccessListener {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for (dataSnapshot in it) {
                    if (dataSnapshot.contains("type") && dataSnapshot["type"] == "offerCandidate") {
                        val offerCandidate = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                offerCandidate["sdpMid"].toString(),
                                Math.toIntExact(offerCandidate["sdpMLineIndex"] as Long),
                                offerCandidate["sdp"].toString()
                            )
                        )
                    } else if (dataSnapshot.contains("type") && dataSnapshot["type"] == "answerCandidate") {
                        val answerCandidate = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                answerCandidate["sdpMid"].toString(),
                                Math.toIntExact(answerCandidate["sdpMLineIndex"] as Long),
                                answerCandidate["sdp"].toString()
                            )
                        )
                    }
                }
                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
            }
        val endCall = hashMapOf(
            "type" to "END_CALL"
        )
        db.collection("calls").document(meetingID)
            .set(endCall)
            .addOnSuccessListener {
                Log.e(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }
        db.collection("calls").document(meetingID)
            .delete()
            .addOnSuccessListener {
                Log.e(TAG, "DocumentSnapshot deleted")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error Document delete", e)
            }

        peerConnection?.close()
    }

    fun enableVideo(videoEnabled: Boolean) {
        if (localVideoTrack != null)
            localVideoTrack?.setEnabled(videoEnabled)
    }

    fun enableAudio(audioEnabled: Boolean) {
        if (localAudioTrack != null)
            localAudioTrack?.setEnabled(audioEnabled)
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }

    fun getPeerConn() = peerConnection

}