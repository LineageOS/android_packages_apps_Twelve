/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.Result.Companion.fold

open class ProviderViewModel(application: Application) : TwelveViewModel(application) {
    private val _providerIdentifier = MutableStateFlow<ProviderIdentifier?>(null)

    /**
     * The provider identifiers to manage.
     */
    protected val providerIdentifier = _providerIdentifier.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val provider = providerIdentifier
        .flatMapLatest {
            it?.let { providerIdentifier ->
                mediaRepository.provider(providerIdentifier).mapLatest { maybeProvider ->
                    maybeProvider?.let { provider ->
                        Result.Success<_, Error>(provider)
                    } ?: Result.Error(Error.NOT_FOUND)
                }
            } ?: flowOf(Result.Loading())
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            Result.Loading()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val canBeManaged = provider
        .mapLatest {
            when (it) {
                is Result.Loading -> null
                is Result.Success -> it.data.type.canBeManaged
                is Result.Error -> false
            }
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val status = provider
        .flatMapLatest { provider ->
            provider.fold(
                onLoading = {
                    flowOf(Result.Loading(it))
                },
                onSuccess = {
                    mediaRepository.status(it)
                },
                onError = {
                    flowOf(Result.Error(it))
                }
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            Result.Loading()
        )

    fun setProviderIdentifier(providerIdentifier: ProviderIdentifier?) {
        _providerIdentifier.value = providerIdentifier
    }

    /**
     * Delete the provider.
     */
    suspend fun deleteProvider() {
        val providerIdentifier = providerIdentifier.value ?: return

        withContext(Dispatchers.IO) {
            mediaRepository.deleteProvider(providerIdentifier)
        }
    }
}
