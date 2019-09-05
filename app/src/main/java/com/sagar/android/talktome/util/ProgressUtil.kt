package com.sagar.android.talktome.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.sagar.android.talktome.R

class ProgressUtil(private val context: Context) {
    private lateinit var dialog: Dialog

    fun show() {
        hide()
        dialog = Dialog(context, R.style.progressBarTheme)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)
        dialog.show()

        UiUtil.hideSoftKeyboard(context)
    }

    fun hide() {
        if (::dialog.isInitialized) {
            if (dialog.isShowing)
                dialog.dismiss()
        }
    }
}