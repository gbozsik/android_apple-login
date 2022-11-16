package com.renegade.applelogin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.renegade.applelogin.util.Const.APPLE_AUTH_URL
import com.renegade.applelogin.util.Const.CLIENT_ID
import com.renegade.applelogin.util.Const.REDIRECT_URI
import com.renegade.applelogin.util.Const.SCOPE
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val appleLoginService = AppleLoginService(this)

        val stateCode = UUID.randomUUID()
        val appleAuthURLFull =
            "${APPLE_AUTH_URL}?response_type=code%20id_token&response_mode=form_post&client_id=${CLIENT_ID}" +
                    "&scope=${SCOPE}&state=${stateCode}&redirect_uri=${REDIRECT_URI}&usePopup=true"

        val appleLogin: Button = findViewById(R.id.appleLogin)
        appleLogin.setOnClickListener { view ->
            Log.i("TAG", "onCreate: Auth url $appleAuthURLFull")
            appleLoginService.openWebViewDialog(appleAuthURLFull)
        }
    }
}