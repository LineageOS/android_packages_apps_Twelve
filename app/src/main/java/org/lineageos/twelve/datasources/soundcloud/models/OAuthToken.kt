/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UriSerializer::class)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.models.OAuthToken.GrantType
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer

/**
 * OAuth token.
 *
 * @param grantType One of [GrantType.AUTHORIZATION_CODE], [GrantType.CLIENT_CREDENTIALS],
 *   [GrantType.REFRESH_TOKEN]
 * @param clientId Client ID
 * @param clientSecret Client secret
 * @param code Authorization code. Required on [grantType] = [GrantType.AUTHORIZATION_CODE]
 * @param redirectUri Redirect URI. Required on [grantType] =
 *   ([GrantType.AUTHORIZATION_CODE]|[GrantType.REFRESH_TOKEN])
 * @param refreshToken Refresh token. Required on [grantType] = [GrantType.REFRESH_TOKEN]
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class OAuthToken(
    @SerialName("grant_type") val grantType: GrantType,
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,
    @SerialName("code") val code: String? = null,
    @SerialName("redirect_uri") val redirectUri: Uri? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
) {
    @Serializable
    enum class GrantType {
        @SerialName("authorization_code")
        AUTHORIZATION_CODE,
        @SerialName("client_credentials")
        CLIENT_CREDENTIALS,
        @SerialName("refresh_token")
        REFRESH_TOKEN,
    }
}
