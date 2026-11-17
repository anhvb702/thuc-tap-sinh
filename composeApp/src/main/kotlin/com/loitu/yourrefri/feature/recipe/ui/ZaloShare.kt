package com.loitu.yourrefri.feature.recipe.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

const val ZALO_PACKAGE = "com.zing.zalo"

fun buildZaloSendIntent(text: String): Intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, text)
    setPackage(ZALO_PACKAGE)
}

fun buildChooserSendIntent(text: String, chooserTitle: String = "Gửi danh sách đi chợ"): Intent =
    Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }, chooserTitle)

fun shareToZaloOrChooser(context: Context, text: String) {
    try {
        context.startActivity(buildZaloSendIntent(text))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(buildChooserSendIntent(text))
    } catch (_: SecurityException) {
        context.startActivity(buildChooserSendIntent(text))
    }
}
