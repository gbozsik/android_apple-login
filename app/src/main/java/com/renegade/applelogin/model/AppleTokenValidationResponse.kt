package com.renegade.applelogin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class AppleTokenValidationResponse(
    @SerializedName(value = "http_status")
    private val httpStatus: Int,
    @SerializedName(value = "access_token")
    private val accessToken: String,
    @SerializedName(value = "token_type")
    private val tokenType: String,
    @SerializedName(value = "expires_in")
    private val expiresIn: Int,
    @SerializedName(value = "refresh_token")
    private val refreshToken: String,
    @SerializedName(value = "id_token")
    private val idToken: String,
    @SerializedName(value = "error")
    private val error: String,
    @SerializedName(value = "error_description")
    private val errorDescription: String
)
