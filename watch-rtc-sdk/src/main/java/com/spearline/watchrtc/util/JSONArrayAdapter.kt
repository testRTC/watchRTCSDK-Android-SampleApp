package com.spearline.watchrtc.util

import com.google.gson.*
import org.json.JSONArray
import org.json.JSONException
import java.lang.reflect.Type


internal class JSONArrayAdapter : JsonSerializer<JSONArray>, JsonDeserializer<JSONArray> {

    companion object {
        val instance = JSONArrayAdapter()
    }

    override fun serialize(
        src: JSONArray?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) {
            return null
        }
        val jsonArray = JsonArray()
        for (i in 0 until src.length()) {
            val `object`: Any = src.opt(i)
            val jsonElement = context!!.serialize(`object`, `object`.javaClass)
            jsonArray.add(jsonElement)
        }
        return jsonArray
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): JSONArray? {
        return if (json == null) {
            null
        } else try {
            JSONArray(json.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            throw JsonParseException(e)
        }
    }
}