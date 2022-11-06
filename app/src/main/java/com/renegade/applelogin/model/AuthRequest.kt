package com.renegade.applelogin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("client_id")
    private val clientId: String,
    @SerializedName("client_secret")
    private val clientSecret: String,
    @SerializedName("code")
    private val code: String,
    @SerializedName("grant_type")
    private val grantType: String
)
