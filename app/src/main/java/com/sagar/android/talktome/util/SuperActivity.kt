package com.sagar.android.talktome.util

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity

@Suppress("unused")
@SuppressLint("Registered")
open class SuperActivity : AppCompatActivity() {

    @Suppress("LeakingThis")
    private val dialogUtil: DialogUtil = DialogUtil(this)

    @Suppress("LeakingThis")
    private val progressUtil: ProgressUtil = ProgressUtil(this)

    fun isConnectedToNetwork() = NetworkUtil.isConnected(this)

    fun showProgress() {
        progressUtil.show()
    }

    fun hideProgress() {
        progressUtil.hide()
    }

    fun showMessageInDialog(message: String) {
        dialogUtil.showMessage(
            message = message
        )
    }
}