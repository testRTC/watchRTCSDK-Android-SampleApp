package com.spearline.watchrtc.model

import com.spearline.watchrtc.util.JsonUtil
import org.json.JSONObject
import java.util.*

data class RTCStatsReport(val report: HashMap<String, RTCStat>, var timestamp: Long) {

    data class RTCStat(
        val timestamp: Long,
        val properties: HashMap<String, Any>
    )

    fun toJson(): String {
        val data = toData()
        return JsonUtil.getGsonInstance().toJson(data)
    }

    fun toData(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        val reportData = HashMap<String, Any>()

        report.entries.forEach {
            val stat = HashMap<String, Any>().apply {
                val inputData = it.value
                val outputData = HashMap<String, Any>().apply {
                    put("timestamp", inputData.timestamp)
                    putAll(inputData.properties)
                }
                putAll(outputData)
            }

            reportData[it.key] = stat
        }
        val timestamp = Date().time
        reportData["timestamp"] = timestamp
        data["report"] = reportData
        data["timestamp"] = timestamp

        return data
    }

    companion object {

        @JvmStatic
        fun fromJson(statsJson: String): RTCStatsReport {
            val report = HashMap<String, RTCStat>()
            val json = JSONObject(statsJson)
            val reportJson = json.getJSONObject("report")

            for (key in reportJson.keys()) {
                if (key != "timestamp") {
                    val jsonStat = reportJson.getJSONObject(key)
                    val timestamp = jsonStat.remove("timestamp") as Long
                    val stat = RTCStat(
                        timestamp,
                        HashMap<String, Any>().apply {
                            for (statKey in jsonStat.keys()) {
                                put(statKey, jsonStat.get(statKey))
                            }
                        }
                    )
                    report[key] = stat
                }
            }

            return RTCStatsReport(report, json.getLong("timestamp"))
        }
    }
}