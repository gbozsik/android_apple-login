package com.renegade.applelogin

import android.app.Dialog
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.acsbendi.requestinspectorwebview.RequestInspectorWebViewClient
import com.acsbendi.requestinspectorwebview.WebViewRequest
import com.renegade.applelogin.api.APIClient
import com.renegade.applelogin.api.RetrofitAPI
import com.renegade.applelogin.util.Const
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.lang.String.format
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


class AppleWebViewClient(
    private val appleWebView: WebView,
    private val mainActivity: MainActivity,
    private val appleDialog: Dialog
) : RequestInspectorWebViewClient(appleWebView) {

    private val usedCodeSet: HashSet<String> = HashSet()

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
            Log.i(TAG, "Redirect received state: $state")
            Log.i(TAG, "Redirect received code: $code")
            if (code != null) {
                getValidationResponseFromApple(code)
            }
        }
        return super.shouldInterceptRequest(view, webViewRequest)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (url?.startsWith(Const.REDIRECT_URI) == true) {
            appleWebView.loadUrl("")
            appleDialog.dismiss()
        } else {
            super.onPageFinished(view, url)
            val displayRectangle = Rect()
            val window = mainActivity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
            val layoutParams = appleWebView.layoutParams
            layoutParams?.height = (displayRectangle.height() * 0.9f).toInt()
            view?.layoutParams = layoutParams
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun getValidationResponseFromApple(code: String) {
        if (usedCodeSet.add(code)) {
            val retrofit = APIClient.getClient()
            val privateKey = getPrivateKey(
                "AuthKey_X5AQH92NHL.p8",
                "EC"
            )
            val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
            val clientSecret = getClientSecret(privateKey)
            Log.i(TAG, "private key: $privateKey")
            Log.i(TAG, "client secret: $clientSecret")
            GlobalScope.launch(Dispatchers.IO) {
                val response = retrofitAPI.authenticateToken(
                    Const.CLIENT_ID,
                    clientSecret,
                    code,
                    "authorization_code"
                )
                if (response.isSuccessful) {
                    Color.parseColor("#00FF00")
                    Log.i(TAG, "data: ${response.body()}")
                    setLoginText("You are in!", Color.parseColor("#00FF00"), 20F)
                } else {
                    setLoginText("Authentication failed!", Color.parseColor("#FF0000"), 20F)
                    Log.i(TAG, "data error: ${response.errorBody()}")
                }
            }
        }
    }

    private fun setLoginText(text: String, color: Int, size: Float) {
        runOnUiThread(Runnable {
            val textView: TextView = mainActivity.findViewById(R.id.loginText)
            textView.text = text
            textView.setTextColor(color)
            textView.textSize = size
        })
    }

    private fun runOnUiThread(runnable: Runnable) {
        mainActivity.runOnUiThread {
            runnable.run()
        }
    }

    private fun getClientSecret(pKey: PrivateKey): String {
        val id = "X5AQH92NHL"
        return Jwts.builder()
            .setHeaderParam(JwsHeader.KEY_ID, id)
            .setHeaderParam(JwsHeader.ALGORITHM, SignatureAlgorithm.ES256)
            .setIssuer("AYXS8ZPVP5")
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 5))
            .setAudience("https://appleid.apple.com")
            .setSubject(Const.CLIENT_ID)
            .signWith(SignatureAlgorithm.ES256, pKey)
            .compact()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getPrivateKey(filename: String, algorithm: String?): PrivateKey {
        val rawPrivateKey = getRawPrivateKeyFromFile(filename)
        try {
            val privateKey: String = rawPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replace("\\s+", "").replace("\n", "")
            val kf: KeyFactory = KeyFactory.getInstance(algorithm)
            return kf.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)))
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, format("Java did not support the algorithm: %s", algorithm), e)
            throw java.lang.RuntimeException("Java did not support the algorithm: %s", e)
        } catch (e: InvalidKeySpecException) {
            Log.e(TAG, "Invalid key format")
            throw java.lang.RuntimeException("Invalid key format")
        }
    }

    private fun getRawPrivateKeyFromFile(filename: String): String {
        var inputStream: InputStream? = null
        val content = StringBuilder()
        try {
            inputStream = mainActivity.baseContext.assets.open(filename)
            val reader = BufferedReader(inputStream.reader())
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                Log.i(TAG, line)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read file", e)
        } finally {
            inputStream?.close()
        }
        return content.toString()
    }
}
