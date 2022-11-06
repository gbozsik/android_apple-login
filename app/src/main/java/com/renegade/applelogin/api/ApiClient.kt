package com.renegade.applelogin.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


internal object APIClient {
    private var retrofit: Retrofit? = null

    //    val client: Retrofit?
    fun getClient(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl("https://appleid.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    }
}