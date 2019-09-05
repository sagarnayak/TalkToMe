package com.sagar.android.talktome.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build


@Suppress("DEPRECATION")
object NetworkUtil {

    fun isConnected(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var result = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(
                connectivityManager.activeNetwork
            )?.let {
                result =
                    when {
                        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
            }
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            result = activeNetwork?.isConnectedOrConnecting == true
        }

        return result
    }
}