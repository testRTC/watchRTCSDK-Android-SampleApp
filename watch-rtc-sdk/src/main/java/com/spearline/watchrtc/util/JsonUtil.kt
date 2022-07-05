package com.spearline.watchrtc.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject

internal object JsonUtil {


    fun getGsonInstance(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(JSONObject::class.java, JSONObjectAdapter.instance)
            .registerTypeAdapter(JSONArray::class.java, JSONArrayAdapter.instance)
            .create()
    }
}