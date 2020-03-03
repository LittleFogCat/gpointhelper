package top.littlefogcat.gpointhelper.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object GsonUtil {
    private val GSON = Gson()

    fun <T> fromJson(json: String, cls: Class<T>): T = GSON.fromJson(json, cls)

    fun <T> fromJson(json: String, type: Type): T = GSON.fromJson(json, type)

    fun toJson(obj: Any): String? = GSON.toJson(obj)

    inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
}