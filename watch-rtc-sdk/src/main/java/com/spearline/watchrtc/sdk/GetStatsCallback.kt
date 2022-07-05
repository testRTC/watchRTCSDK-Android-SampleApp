package com.spearline.watchrtc.sdk

import com.spearline.watchrtc.model.RTCStatsReport

interface GetStatsCallback {

    fun onStatsAvailable(stats: RTCStatsReport)
}