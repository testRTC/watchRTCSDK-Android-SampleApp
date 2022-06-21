package com.spearline.webrtc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.gson.Gson
import com.spearline.utils.WatchRTCUtils
import com.spearline.watchrtc.sdk.GetStatsCallback
import com.spearline.watchrtc.sdk.RtcDataProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.webrtc.*


class RTCActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient

    private val audioManager by lazy { RTCAudioManager.create(this) }

    private var meetingID: String = "test-call"

    private var isJoin = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent.hasExtra("meetingID"))
            meetingID = intent.getStringExtra("meetingID")!!
        if (intent.hasExtra("isJoin"))
            isJoin = intent.getBooleanExtra("isJoin", false)


        WatchRTCUtils.init(rtcDataProvider)
        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        switch_camera_button.setOnClickListener {
            rtcClient.switchCamera()
        }

        audio_output_button.setOnClickListener {
            if (inSpeakerMode) {
                inSpeakerMode = false
                audio_output_button.setImageResource(R.drawable.ic_baseline_hearing_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                inSpeakerMode = true
                audio_output_button.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
        }
        video_button.setOnClickListener {
            if (isVideoPaused) {
                isVideoPaused = false
                video_button.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            } else {
                isVideoPaused = true
                video_button.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient.enableVideo(isVideoPaused)
        }
        mic_button.setOnClickListener {
            if (isMute) {
                isMute = false
                mic_button.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                isMute = true
                mic_button.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient.enableAudio(isMute)
        }
        end_call_button.setOnClickListener {
            rtcClient.endCall(meetingID)
            WatchRTCUtils.disconnect()
            remote_view.isGone = false
            Constants.isCallEnded = true
            finish()
            val intent = Intent(this@RTCActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, AUDIO_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted() {
        rtcClient = RTCClient(
            application,
            meetingID,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    signallingClient.sendIceCandidate(p0, isJoin)
                    rtcClient.addIceCandidate(p0)
//                    WatchRTCUtils.logEvents(
//                        1,
//                        "onicecandidate",
//                        Gson().toJson(p0)
//                    )
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    WatchRTCUtils.logEvents(
                        "oniceconnectionstatechange",
                        p0
                    )
                }

                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                    super.onIceGatheringChange(p0)
                    val jsonObject = JSONObject()
                    jsonObject.put("status", p0.toString())
                    WatchRTCUtils.logEvents(
                        "onicegatheringstatechange",
                        p0
                    )
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {

                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {

                    if (!TextUtils.isEmpty(newState.toString())) {
                        when (newState.toString()) {
                            "CONNECTING" -> callStart()
//                            "CONNECTED" -> callStart()
                            "CLOSED" -> callStop()
                        }
                    }
                    WatchRTCUtils.logEvents(
                        "onconnectionstatechange",
                        newState
                    )
                }

                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                    super.onSignalingChange(p0)
                    val jsonObject = JSONObject()
                    jsonObject.put("status", p0.toString())
                    WatchRTCUtils.logEvents(
                        "onsignalingstatechange",
                        p0
                    )
                }

            }
        )

        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)
        signallingClient = SignalingClient(meetingID, createSignallingClientListener())
        if (!isJoin)
            rtcClient.call(sdpObserver, meetingID)
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            end_call_button.isClickable = true
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            rtcClient.answer(sdpObserver, meetingID)
            remote_view_loading.isGone = true
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            remote_view_loading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(meetingID)
                finish()
                startActivity(Intent(this@RTCActivity, MainActivity::class.java))
            }
        }
    }

    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
            !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION),
                CAMERA_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera And Audio Permission Required")
            .setMessage("This app need the camera and audio to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAndAudioPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        signallingClient.destroy()
        super.onDestroy()
    }

    private fun callStart() {

    }

    private fun callStop() {
        WatchRTCUtils.disconnect()
    }

    private val rtcDataProvider = object : RtcDataProvider {
        override fun getStats(callback: GetStatsCallback) {
            val peerCon = rtcClient.getPeerConn()
            peerCon?.getStats { rtcStatsReport ->
                callback.onStatsAvailable(mapStats(rtcStatsReport))
            }
        }
    }

    private fun mapStats(stats: RTCStatsReport): com.spearline.watchrtc.model.RTCStatsReport {
        val report = HashMap<String, com.spearline.watchrtc.model.RTCStatsReport.RTCStat>()
        val jsonStr = Gson().toJson(stats)
        val tempJson = JSONObject(jsonStr)
        if (tempJson.has("stats")) {
            val statsJson = tempJson.optJSONObject("stats")
            if (statsJson != null) {
                for (key in statsJson.keys()) {

                    val stat = statsJson.getJSONObject(key)
                    val statProperties = HashMap<String, Any>()
                    val membersJson = stat.getJSONObject("members")
                    for (memberJsonKey in membersJson.keys()) {
                        statProperties[memberJsonKey] = membersJson.get(memberJsonKey)
                    }
                    val watchRtcStat = com.spearline.watchrtc.model.RTCStatsReport.RTCStat(
                        stat.getLong("timestampUs"),
                        statProperties
                    )
                    report[key] = watchRtcStat
                }
            }
        }

        return com.spearline.watchrtc.model.RTCStatsReport(report, stats.timestampUs.toLong())
    }
}