package com.harera.common.ui

import android.app.AlertDialog
import android.content.Context
import com.harera.common.R

class LoadingDialog(context: Context) {
    var dialog: AlertDialog = AlertDialog.Builder(context)
        .setView(R.layout.loading_dialog)
        .setCancelable(false)
        .create()

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.cancel()
    }
}