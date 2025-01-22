/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

class ProviderIdentifier(private val providerType: ProviderType, private val providerTypeId: Long) {
    fun matches(provider: Provider): Boolean {
        return providerType == provider.type && providerTypeId == provider.typeId
    }

    fun toJson(): String {
        return """{"providerType":"${providerType.name}","providerTypeId":$providerTypeId}"""
    }

    companion object {
        fun fromJson(json: String): ProviderIdentifier {
            val providerTypeRegex = """"providerType":"(.*?)"""".toRegex()
            val providerTypeIdRegex = """"providerTypeId":(\d+)""".toRegex()

            val providerType = providerTypeRegex.find(json)?.groupValues?.get(1)?.let {
                ProviderType.valueOf(it)
            } ?: ProviderType.LOCAL
            val providerTypeId = providerTypeIdRegex.find(json)?.groupValues?.get(1)?.toLong() ?: 0

            return ProviderIdentifier(providerType, providerTypeId)
        }
    }
}
