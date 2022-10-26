package com.renegade.applelogin

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.webkit.WebView
import java.util.*

class AppleLoginService(private val mainActivity: MainActivity) {

    lateinit var appledialog: Dialog
    lateinit var stateCode: UUID

    @SuppressLint("SetJavaScriptEnabled")
    fun openWebViewDialog(url: String) {
        appledialog = Dialog(mainActivity)
        val appleWebView = getAppleWebView(appledialog)
        appleWebView.loadUrl(url)
        appledialog.setContentView(appleWebView)
        appledialog.show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getAppleWebView(appledialog: Dialog): WebView {
        val appleWebView = WebView(mainActivity)
        appleWebView.isVerticalFadingEdgeEnabled
        appleWebView.isHorizontalScrollBarEnabled = false
        appleWebView.isVerticalScrollBarEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appleWebView.webViewClient = AppleWebViewClient(appleWebView, mainActivity, appledialog)
        }
        appleWebView.settings.javaScriptEnabled = true
        return appleWebView
    }
}