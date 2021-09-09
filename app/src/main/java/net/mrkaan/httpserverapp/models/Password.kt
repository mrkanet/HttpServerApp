package net.mrkaan.httpserverapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Entity
data class Password(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "username") var userName: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "last_update") var lastUpdate: Long,
    @ColumnInfo(name = "grouping") var grouping: String,
    @ColumnInfo(name = "old_passwords") var oldPasswords: String
) {
    companion object {
        fun getPasswordFromJson(param: JSONObject): Password {
            //todo check if is website saved before with same username if yes then update it and update old_passwords
            val newUid = UUID.randomUUID().toString()
            val url = param.getString("url")
            val userName = param.getString("username")
            val password = param.getString("password")
            val grouping = param.getString("grouping")
            val lastUpdate = Date().time
            val oldPasswords = ""
            return Password(
                newUid,
                url,
                userName,
                password,
                lastUpdate,
                grouping,
                oldPasswords
            )
        }

        fun toJsonArray(params: List<Password>): JSONArray {
            val array = JSONArray()
            for (p in params) {
                val obj = JSONObject()
                obj.put("url", p.url)
                obj.put("username", p.userName)
                obj.put("password", p.password)
                obj.put("grouping", p.grouping)
                obj.put("lastupdate", p.lastUpdate)
                if (p.oldPasswords.isNotEmpty()) {
                    obj.put("oldpasswords", p.oldPasswords)
                }
                array.put(obj)
            }
            return array
        }
    }

}