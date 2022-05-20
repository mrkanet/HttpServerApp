package net.mrkaan.httpserverapp.utils

import org.json.JSONObject

class SafeJsonObject(json: String) : JSONObject(json) {
    var defaultString = ""

    override fun getString(name: String): String {
        return try {
            super.getString(name)
        } catch (e: Exception) {
            defaultString
        }
    }
}