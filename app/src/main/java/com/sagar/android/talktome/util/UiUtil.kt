package com.sagar.android.talktome.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * created by SAGAR KUMAR NAYAK
 * util class to perform any kind of UI operations
 *
 *
 * exposed methods
 * [.hideSoftKeyboard]
 * [.hideSoftKeyboardAtStart]
 */
@Suppress("unused")
object UiUtil {

    /**
     * method to hide the soft keyboard
     *
     * @param context context
     */
    fun hideSoftKeyboard(context: Context) {
        val view = (context as Activity).currentFocus
        if (view != null) {
            val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * method to set the activity to not to show the soft keyboard at activity start.
     * to use this method call it before [Activity.setContentView] in Activity
     *
     * @param context context
     */
    fun hideSoftKeyboardAtStart(context: Context) {
        (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    /**
     * method to set the activity to stay wake when running. this will not allow the android device
     * to go to sleep when the activity is running.
     *
     * @param context activity context
     */
    fun doNotAllowForSleep(context: Context) {
        (context as Activity).window
            .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
