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
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.entity.UrlEncodedFormEntity
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.protocol.HTTP
import com.renegade.applelogin.api.RetrofitAPI
import com.renegade.applelogin.model.AppleTokenValidationResponse
import com.renegade.applelogin.model.AuthRequest
import com.renegade.applelogin.util.Const
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.UnsupportedEncodingException
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
            if (code != null) {
                getValidationResponseFromApple(code)
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun getValidationResponseFromApple(code: String) {
        //TODO build it in Retrofi
        val retrofit = Retrofit.Builder()
            .baseUrl("https://appleid.apple.com")
            .addConverterFactory(GsonConverterFactory.create()) // at last we are building our retrofit builder.
            .build()
        // below line is to create an instance for our retrofit api class.
        val privateKey = getPrivateKey("secret/AuthKey_8283WJR7GJ.p8", "EC")
        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
//        val post: HttpPost = getAppleTokenValidationRequest(code, clientSecret)
//        return sendValidationRequestToApple(post)
        val clientSecret = getClientSecret(privateKey)
        Log.i(TAG, "private key: $privateKey")
        Log.i(TAG, "client secret: $clientSecret")
        val call: Call<AppleTokenValidationResponse?>? =
            retrofitAPI.authenticateToken(
                Const.CLIENT_ID,
                clientSecret,
                code,
                "authorization_code"
            )
        if (call != null) {
            return call.enqueue(object : Callback<AppleTokenValidationResponse?> {
                override fun onResponse(
                    call: Call<AppleTokenValidationResponse?>,
                    response: Response<AppleTokenValidationResponse?>
                ) {
                    if (response.isSuccessful) {
                        var resp = response.body()
                        Log.i(TAG, "body: $resp")
                    } else {
                        Log.i(TAG, "onResponse: $response")
                        Log.i(TAG, "onResponse: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<AppleTokenValidationResponse?>, t: Throwable) {
                    Log.i(TAG, "on: failure $t")
                }
            })
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getAppleTokenValidationRequest(code: String, clientSecret: String): HttpPost {
        val post = HttpPost("https://appleid.apple.com/auth/token")
        val nvps: MutableList<NameValuePair> = ArrayList()
        nvps.add(BasicNameValuePair("client_id", "com.renegade.android.login"))
        nvps.add(BasicNameValuePair("client_secret", clientSecret))
        nvps.add(BasicNameValuePair("code", code))
        nvps.add(BasicNameValuePair("grant_type", "authorization_code"))
        post.setEntity(
            UrlEncodedFormEntity(
                nvps,
                HTTP.UTF_8
            )
        )
        return post
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
    fun getPrivateKey(filename: String?, algorithm: String?): PrivateKey {
//        val authFileContent = String(Files.readAllBytes(Paths.get("/home/gabor/AndroidStudioProjects/AppleLogin/app/secret/AuthKey_8283WJR7GJ.p8")), Charsets.UTF_8)
        val authFileContent: String =
            "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgvv8pqSm1wZdQYsgT+vUY+3Zr86vR1WafAnI7YJ1ls/CgCgYIKoZIzj0DAQehRANCAATNz+hKXzTZD18ZhCoLpiAQy2cxcf1CWBsM4CcocVrPn/aUn2Beq0QfANzhSY9TlQ5pEM9LxSF8PNk6y1n5ADbQ"
//            "-----BEGIN PRIVATE KEY-----\n" +
//        "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgvv8pqSm1wZdQYsgT\n" +
//                "+vUY+3Zr86vR1WafAnI7YJ1ls/CgCgYIKoZIzj0DAQehRANCAATNz+hKXzTZD18Z\n" +
//                "hCoLpiAQy2cxcf1CWBsM4CcocVrPn/aUn2Beq0QfANzhSY9TlQ5pEM9LxSF8PNk6\n" +
//                "y1n5ADbQ\n" +
//                "-----END PRIVATE KEY-----";
        try {
            val privateKey: String = authFileContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replace("\\s+", "").replace("\\n", "")
            val kf: KeyFactory = KeyFactory.getInstance(algorithm);
            return kf.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, format("Java did not support the algorithm: %s", algorithm), e)
            throw java.lang.RuntimeException("Java did not support the algorithm: %s", e)
        } catch (e: InvalidKeySpecException) {
            Log.e(TAG, "Invalid key format")
            throw java.lang.RuntimeException("Invalid key format")
        }
    }
}
