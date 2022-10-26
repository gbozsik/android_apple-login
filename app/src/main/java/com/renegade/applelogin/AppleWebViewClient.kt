package com.renegade.applelogin

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.acsbendi.requestinspectorwebview.RequestInspectorWebViewClient
import com.acsbendi.requestinspectorwebview.WebViewRequest
import com.renegade.applelogin.util.Const

class AppleWebViewClient(
    private val appleWebView: WebView,
    private val mainActivity: MainActivity,
    private val appledialog: Dialog
) : RequestInspectorWebViewClient(appleWebView) {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        Log.i(TAG, "My onPageStarted. URL: $url")

        super.onPageStarted(view, url, favicon)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun shouldInterceptRequest(
        view: WebView,
        webViewRequest: WebViewRequest
    ): WebResourceResponse? {
        Log.i(TAG, "Redirect received state: ${webViewRequest.body}")
        if (webViewRequest.url.startsWith(Const.REDIRECT_URI) && webViewRequest.body.isNotEmpty() && webViewRequest.body.isNotBlank()) {
            val body = webViewRequest.body
            val bodyAsUri = "https://www.fake.com?$body"
            Log.i(TAG, "Redirect received state: ${webViewRequest.body}")
            val uri = Uri.parse(bodyAsUri)
            val state = uri.getQueryParameter("state")
            val code = uri.getQueryParameter("code")
//            if (state == stateCode.toString()) {
                //todo do the login stuff
            Log.i(TAG, "Redirect received state: $state")
            Log.i(TAG, "Redirect received code: $code")
//            }
        }
        return super.shouldInterceptRequest(view, webViewRequest)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (url?.startsWith(Const.REDIRECT_URI) == true) {
            appleWebView.loadUrl("")
            appledialog.dismiss()
        } else {
            super.onPageFinished(view, url)
            // retrieve display dimensions
            val displayRectangle = Rect()
            val window = mainActivity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
            val layoutParams = appleWebView.layoutParams
            layoutParams?.height = (displayRectangle.height() * 0.9f).toInt()
            view?.layoutParams = layoutParams
        }
    }
}