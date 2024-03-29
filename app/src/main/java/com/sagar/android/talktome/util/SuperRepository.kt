package com.sagar.android.talktome.util

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

@Suppress("unused")
open class SuperRepository {

    //util function
    fun getErrorMessage(throwable: Throwable): String {
        return if (throwable is HttpException) {
            val responseBody = throwable.response()!!.errorBody()
            try {
                val jsonObject = JSONObject(responseBody!!.string())
                jsonObject.getString("error")
            } catch (e: Exception) {
                e.message!!
            }
        } else (when (throwable) {
            is SocketTimeoutException -> "Timeout occurred"
            is IOException -> "network error"
            else -> throwable.message
        })!!
    }

    fun getErrorMessage(responseBody: ResponseBody): String {
        return try {
            val jsonObject = JSONObject(responseBody.string())
            jsonObject.getString("error")
        } catch (e: Exception) {
            "Something went wrong."
        }
    }
}