package com.yara.raco.model.user

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AccessToken(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("scope")
    val scope: String
)
