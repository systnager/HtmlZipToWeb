package com.bohdan2505.htmlziptoweb

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import java.io.File


class JavaScriptInterface(private val context: Context) {
    @JavascriptInterface
    fun copyToClipboard(value: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", value)
        clipboardManager!!.setPrimaryClip(clipData)
    }

    @JavascriptInterface
    fun downloadFile(fileContent: String?, fileName: String?) {
        val fileSystem = FileSystem()
        if (fileContent != null) {
            fileSystem.writeToFile(File("sdcard/Download/$fileName"), fileContent)
        }
    }
}

