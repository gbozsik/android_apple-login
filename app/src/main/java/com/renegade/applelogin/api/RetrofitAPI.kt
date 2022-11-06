package com.renegade.applelogin.api

import com.renegade.applelogin.model.AppleTokenValidationResponse
import com.renegade.applelogin.model.AuthRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

import retrofit2.http.POST

interface RetrofitAPI {

    @FormUrlEncoded
    @POST("/auth/token")
    fun authenticateToken(@Field("client_id") clientId: String,
                          @Field("client_secret") clientSecret: String,
                          @Field("code") code: String,
                          @Field("grant_type") grandType: String): Call<AppleTokenValidationResponse?>?
}