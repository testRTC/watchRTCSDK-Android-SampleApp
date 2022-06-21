package com.spearline.webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.spearline.watchrtc.util.WatchRtcDebugLogger
import kotlinx.android.synthetic.main.activity_debug.*

class DebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        callList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        callList.adapter = CallListAdapter(object : CallListAdapter.OnCallClickListener {
            override fun onClick(callLog: String?) {
                logs.text = callLog
                logs.visibility = View.VISIBLE
                callList.visibility = View.GONE
            }
        })

        logs.setOnClickListener {
            it.visibility = View.GONE
            callList.visibility = View.VISIBLE
        }
    }
}