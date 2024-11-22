/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus

@OptIn(ExperimentalCoroutinesApi::class)
class ManageProviderViewModel(application: Application) : ProvidersViewModel(application) {
    /**
     * The provider identifiers to manage.
     */
    private val providerIds = MutableStateFlow<Pair<ProviderType, Long>?>(null)

    /**
     * The user defined provider type. The one in [providerIds] will always take
     * precedence over this.
     */
    private val _selectedProviderType = MutableStateFlow<ProviderType?>(null)

    /**
     * Whether we're managing an existing provider or adding a new one.
     */
    val inEditMode = providerIds
        .mapLatest { it != null }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * The provider to manage.
     */
    val provider = providerIds.flatMapLatest {
        it?.let { providerIds ->
            mediaRepository.provider(
                providerIds.first, providerIds.second
            ).mapLatest { maybeProvider ->
                maybeProvider?.let { provider ->
                    RequestStatus.Success<_, MediaError>(provider)
                } ?: RequestStatus.Error(MediaError.NOT_FOUND)
            }
        } ?: flowOf(RequestStatus.Success(null))
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Success(null)
        )

    /**
     * The [Bundle] containing the arguments of the provider to manage.
     */
    private val providerArguments = providerIds
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.providerArguments(it.first, it.second)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The provider type.
     */
    private val providerType = combine(
        _selectedProviderType,
        provider,
    ) { selectedProviderType, provider ->
        when (provider) {
            is RequestStatus.Success -> {
                provider.data?.type
            }

            else -> null
        } ?: selectedProviderType
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The provider type and the arguments of the provider to manage.
     */
    val providerTypeWithArguments = combine(
        providerType,
        providerArguments,
    ) { providerType, providerArguments ->
        providerType to providerArguments
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null to null
        )

    /**
     * Set the provider to manage. When null, we're adding a new provider.
     */
    fun setProvider(provider: Pair<ProviderType, Long>?) {
        providerIds.value = provider
    }

    fun setProviderType(providerType: ProviderType?) {
        _selectedProviderType.value = providerType
    }

    /**
     * Add a new provider.
     */
    suspend fun addProvider(
        providerType: ProviderType, name: String, arguments: Bundle
    ) {
        withContext(Dispatchers.IO) {
            mediaRepository.addProvider(providerType, name, arguments)
        }
    }

    /**
     * Update the provider.
     */
    suspend fun updateProvider(name: String, arguments: Bundle) {
        val (providerType, providerTypeId) = providerIds.value ?: return

        withContext(Dispatchers.IO) {
            mediaRepository.updateProvider(providerType, providerTypeId, name, arguments)
        }
    }

    /**
     * Delete the provider.
     */
    suspend fun deleteProvider() {
        val (providerType, providerTypeId) = providerIds.value ?: return

        withContext(Dispatchers.IO) {
            mediaRepository.deleteProvider(providerType, providerTypeId)
        }
    }
}
