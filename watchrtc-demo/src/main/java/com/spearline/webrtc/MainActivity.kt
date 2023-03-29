package com.spearline.webrtc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spearline.webrtc.databinding.ActivityStartBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize watch rtc
//        WatchRTCUtils.init(this)

        Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        binding.meetingId.setText("${binding.meetingId.text}:${getRandomNumber()}")
        binding.startMeeting.setOnClickListener {

            /* val webrtcTest = WebrtcTest(this)
             webrtcTest.startTesting()*/

            if (binding.meetingId.text.toString().trim().isNullOrEmpty())
                binding.meetingId.error = "Please enter meeting id"
            else {
                val temp = binding.radioGroup.checkedRadioButtonId
                val isServer = (temp == R.id.radioServer)

                Thread.sleep(2500)

                db.collection("calls")
                    .document(binding.meetingId.text.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it["type"] == "OFFER" || it["type"] == "ANSWER" || it["type"] == "END_CALL") {
                            binding.meetingId.error = "Please enter new meeting ID"
                        } else {
                            val intent = Intent(this@MainActivity, RTCActivity::class.java)
                            intent.putExtra("meetingID", binding.meetingId.text.toString())
                            intent.putExtra("isJoin", false)
                            intent.putExtra(Constants.isServer, isServer)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        binding.meetingId.error = "Please enter new meeting ID"
                    }
            }
        }
        binding.joinMeeting.setOnClickListener {
            if (binding.meetingId.text.toString().trim().isNullOrEmpty())
                binding.meetingId.error = "Please enter meeting id"
            else {
                val temp = binding.radioGroup.checkedRadioButtonId
                val isServer = (temp == R.id.radioServer)

                Thread.sleep(5000)

                val intent = Intent(this@MainActivity, RTCActivity::class.java)
                intent.putExtra("meetingID", binding.meetingId.text.toString())
                intent.putExtra("isJoin", true)
                intent.putExtra(Constants.isServer, isServer)
                startActivity(intent)
            }
        }
    }

    private fun getRandomNumber(): Int {
        val rand = Random()
        val maxNumber = 1000
        return rand.nextInt(maxNumber) + 1
    }

}