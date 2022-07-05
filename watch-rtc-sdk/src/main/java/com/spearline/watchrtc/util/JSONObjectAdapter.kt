package com.spearline.watchrtc.util

import com.google.gson.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type


internal class JSONObjectAdapter : JsonSerializer<JSONObject>, JsonDeserializer<JSONObject> {

    companion object{
        val instance = JSONObjectAdapter()
    }

    override fun serialize(
        src: JSONObject?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) {
            return null
        }

        val jsonObject = JsonObject()
        val keys: Iterator<String> = src.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value: Any = src.opt(key)
            val jsonElement = context!!.serialize(value, value.javaClass)
            jsonObject.add(key, jsonElement)
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): JSONObject? {
        return if (json == null) {
            null
        } else try {
            JSONObject(json.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            throw JsonParseException(e)
        }
    }
}