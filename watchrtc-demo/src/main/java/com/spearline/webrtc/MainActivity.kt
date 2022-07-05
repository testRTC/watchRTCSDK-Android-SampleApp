package com.spearline.webrtc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_start.*

class MainActivity : AppCompatActivity() {
    //https://proandroiddev.com/webrtc-sample-in-kotlin-e584681ed7fc
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        //Initialize watch rtc
//        WatchRTCUtils.init(this)

        Constants.isIntiatedNow = true
        Constants.isCallEnded = true
        start_meeting.setOnClickListener {

            /* val webrtcTest = WebrtcTest(this)
             webrtcTest.startTesting()*/

            if (meeting_id.text.toString().trim().isNullOrEmpty())
                meeting_id.error = "Please enter meeting id"
            else {
                val temp = radioGroup.checkedRadioButtonId
                val isServer = (temp == R.id.radioServer)

                Thread.sleep(5000)

                db.collection("calls")
                    .document(meeting_id.text.toString())
                    .get()
                    .addOnSuccessListener {
                        if (it["type"] == "OFFER" || it["type"] == "ANSWER" || it["type"] == "END_CALL") {
                            meeting_id.error = "Please enter new meeting ID"
                        } else {
                            val intent = Intent(this@MainActivity, RTCActivity::class.java)
                            intent.putExtra("meetingID", meeting_id.text.toString())
                            intent.putExtra("isJoin", false)
                            intent.putExtra(Constants.isServer, isServer)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        meeting_id.error = "Please enter new meeting ID"
                    }
            }
        }
        join_meeting.setOnClickListener {
            if (meeting_id.text.toString().trim().isNullOrEmpty())
                meeting_id.error = "Please enter meeting id"
            else {
                val temp = radioGroup.checkedRadioButtonId
                val isServer = (temp == R.id.radioServer)

                Thread.sleep(5000)

                val intent = Intent(this@MainActivity, RTCActivity::class.java)
                intent.putExtra("meetingID", meeting_id.text.toString())
                intent.putExtra("isJoin", true)
                intent.putExtra(Constants.isServer, isServer)
                startActivity(intent)
            }
        }
    }
}