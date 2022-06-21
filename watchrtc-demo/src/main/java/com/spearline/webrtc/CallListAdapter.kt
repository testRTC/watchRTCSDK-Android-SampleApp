package com.spearline.webrtc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spearline.watchrtc.util.WatchRtcDebugLogger

class CallListAdapter(private val itemCLickListener: OnCallClickListener) :
    RecyclerView.Adapter<CallListAdapter.CallViewHolder>() {

    class CallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        return CallViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.call_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val textView: TextView = holder.itemView as TextView
        textView.text = WatchRtcDebugLogger.callList()[position]
        textView.setOnClickListener {
            itemCLickListener.onClick(WatchRtcDebugLogger.viewByIndex(position))
        }
    }

    override fun getItemCount(): Int {
        return WatchRtcDebugLogger.callsCount()
    }

    interface OnCallClickListener {
        fun onClick(callLog: String?)
    }
}