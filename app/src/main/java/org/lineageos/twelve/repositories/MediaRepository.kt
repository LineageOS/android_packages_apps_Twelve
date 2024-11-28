/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import okhttp3.Cache
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.datasources.DummyDataSource
import org.lineageos.twelve.datasources.LocalDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.datasources.SubsonicDataSource
import org.lineageos.twelve.ext.JOIN_LOCAL_DEVICES_KEY
import org.lineageos.twelve.ext.joinLocalDevices
import org.lineageos.twelve.ext.preferenceFlow
import org.lineageos.twelve.ext.storageVolumesFlow
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

/**
 * Media repository. This class coordinates all the providers and their data source.
 * All methods that involves a URI as a parameter will be redirected to the
 * proper data source that can handle the media item. Methods that just returns a list of things
 * will be redirected to the provider selected by the user (see [navigationProvider]).
 * If the navigation provider disappears, the local provider will be used as a fallback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepository(
    context: Context,
    scope: CoroutineScope,
    private val database: TwelveDatabase,
) {
    // System services
    private val storageManager = context.getSystemService(StorageManager::class.java)

    /**
     * Content resolver.
     */
    private val contentResolver = context.contentResolver

    /**
     * Shared preferences.
     */
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Local data source singleton.
     */
    private val localDataSource = LocalDataSource(
        contentResolver,
        MediaStore.VOLUME_EXTERNAL,
        database
    )

    /**
     * Local provider singleton.
     */
    private val localProvider = Provider(
        ProviderType.LOCAL,
        LOCAL_PROVIDER_ID,
        Build.MODEL,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaStoreProviders = sharedPreferences.preferenceFlow(
        JOIN_LOCAL_DEVICES_KEY, SharedPreferences::joinLocalDevices
    )
        .flatMapLatest { joinLocalDevices ->
            if (joinLocalDevices) {
                flowOf(listOf(localProvider to localDataSource))
            } else {
                storageManager.storageVolumesFlow().mapLatest { storageVolumes ->
                    storageVolumes.toMutableList().partition { volume ->
                        volume.mediaStoreVolumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY
                    }.let { it.first + it.second }.mapNotNull { storageVolume ->
                        storageVolume.mediaStoreVolumeName?.let { mediaStoreVolumeName ->
                            Provider(
                                ProviderType.LOCAL,
                                storageVolume.directory.hashCode().toLong(),
                                storageVolume.getDescription(context),
                            ) to LocalDataSource(
                                contentResolver,
                                mediaStoreVolumeName,
                                database,
                            )
                        }
                    }
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(),
            listOf()
        )

    /**
     * HTTP cache
     * 50 MB should be enough for most cases.
     */
    private val cache = Cache(context.cacheDir, 50 * 1024 * 1024)

    /**
     * All the providers. This is our single point of truth for the providers.
     */
    private val allProvidersToDataSource = combine(
        mediaStoreProviders,
        database.getSubsonicProviderDao().getAll().mapLatest { subsonicProviders ->
            subsonicProviders.map {
                val arguments = bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )

                Provider(
                    ProviderType.SUBSONIC,
                    it.id,
                    it.name,
                ) to SubsonicDataSource(arguments, cache)
            }
        }
    ) { providers -> providers.toList().flatten() }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(),
        )

    /**
     * All providers available to the app.
     */
    val allProviders = allProvidersToDataSource.mapLatest {
        it.map { (provider, _) -> provider }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(),
        )

    /**
     * The current navigation provider's identifiers.
     */
    private val _navigationProvider = MutableStateFlow<Pair<ProviderType, Long>?>(null)

    /**
     * The current navigation provider and its data source.
     */
    private val navigationProviderToDataSource = combine(
        _navigationProvider,
        allProvidersToDataSource,
    ) { navigationProvider, allProvidersToDataSource ->
        navigationProvider?.let {
            allProvidersToDataSource.firstOrNull { (provider, _) ->
                provider.type == it.first && provider.typeId == it.second
            }
        } ?: allProvidersToDataSource.firstOrNull()
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            null,
        )

    /**
     * The current navigation provider. This is used when the user looks for all media types,
     * like the home page, or with the search feature. In case the selected one disappears, the
     * repository will automatically fallback to the first provider available (this usually being
     * the local provider). If no provider is available, this will return null.
     */
    val navigationProvider = navigationProviderToDataSource
        .mapLatest { it?.first }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            null,
        )

    /**
     * The current navigation provider's data source. Even when no provider is available, a dummy
     * data source will be used.
     */
    private val navigationDataSource = navigationProviderToDataSource
        .mapLatest { it?.second ?: DummyDataSource }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            DummyDataSource,
        )

    /**
     * Given a media item, get a flow of the provider that handles these media items' URIs.
     * All URIs must be supported by the same provider to get a valid result.
     *
     * @param uris The media items' URIs
     * @return A flow of the provider that handles these media items' URIs.
     */
    fun providerOfMediaItems(vararg uris: Uri) = allProvidersToDataSource.mapLatest {
        it.firstOrNull { (_, dataSource) ->
            uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
        }?.first
    }

    /**
     * Given a media item, get the provider that handles these media items' URIs.
     * All URIs must be supported by the same provider to get a valid result.
     *
     * @param uris The media items' URIs
     * @return The provider that handles these media items' URIs.
     */
    fun getProviderOfMediaItems(
        vararg uris: Uri
    ) = allProvidersToDataSource.value.firstOrNull { (_, dataSource) ->
        uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
    }?.first

    /**
     * Get a flow of the [Provider].
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     * @return A flow of the corresponding [Provider].
     */
    fun provider(providerType: ProviderType, providerTypeId: Long) = allProviders.mapLatest {
        it.firstOrNull { provider ->
            providerType == provider.type && providerTypeId == provider.typeId
        }
    }

    /**
     * Get a flow of the [Bundle] containing the arguments. This method should only be used by the
     * provider manager fragment.
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     * @return A flow of [Bundle] containing the arguments.
     */
    fun providerArguments(providerType: ProviderType, providerTypeId: Long) = when (providerType) {
        ProviderType.LOCAL -> flowOf(Bundle.EMPTY)

        ProviderType.SUBSONIC -> database.getSubsonicProviderDao().getById(
            providerTypeId
        ).mapLatest { subsonicProvider ->
            subsonicProvider?.let {
                bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )
            }
        }
    }

    /**
     * Add a new provider to the database.
     *
     * @param providerType The [ProviderType]
     * @param name The name of the new provider
     * @param arguments The arguments of the new provider. They must have been validated beforehand
     * @return A [Pair] containing the [ProviderType] and the ID of the new provider. You can then
     *   use those values to retrieve the new [Provider]
     */
    suspend fun addProvider(
        providerType: ProviderType, name: String, arguments: Bundle
    ) = when (providerType) {
        ProviderType.LOCAL -> throw Exception("Cannot create local providers")

        ProviderType.SUBSONIC -> {
            val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
            val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
            val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
            val useLegacyAuthentication = arguments.requireArgument(
                SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
            )

            val typeId = database.getSubsonicProviderDao().create(
                name, server, username, password, useLegacyAuthentication
            )

            providerType to typeId
        }
    }

    /**
     * Update an already existing provider.
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     * @param name The updated name
     * @param arguments The updated arguments
     */
    suspend fun updateProvider(
        providerType: ProviderType,
        providerTypeId: Long,
        name: String,
        arguments: Bundle
    ) {
        when (providerType) {
            ProviderType.LOCAL -> throw Exception("Cannot update local providers")

            ProviderType.SUBSONIC -> {
                val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
                val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
                val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
                val useLegacyAuthentication = arguments.requireArgument(
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
                )

                database.getSubsonicProviderDao().update(
                    providerTypeId,
                    name,
                    server,
                    username,
                    password,
                    useLegacyAuthentication,
                )
            }
        }
    }

    /**
     * Delete a provider.
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     */
    suspend fun deleteProvider(providerType: ProviderType, providerTypeId: Long) {
        when (providerType) {
            ProviderType.LOCAL -> throw Exception("Cannot delete local providers")

            ProviderType.SUBSONIC -> database.getSubsonicProviderDao().delete(providerTypeId)
        }
    }

    /**
     * Change the default navigation provider. In case this provider disappears the repository will
     * automatically fallback to the local provider.
     *
     * @param provider The new navigation provider
     */
    fun setNavigationProvider(provider: Provider) {
        _navigationProvider.value = provider.type to provider.typeId
    }

    /**
     * @see MediaDataSource.mediaTypeOf
     */
    suspend fun mediaTypeOf(mediaItemUri: Uri) = withMediaItemsDataSource(mediaItemUri) {
        mediaTypeOf(mediaItemUri)
    }

    /**
     * @see MediaDataSource.activity
     */
    fun activity() = navigationDataSource.flatMapLatest { it.activity() }

    /**
     * @see MediaDataSource.albums
     */
    fun albums(
        sortingRule: SortingRule = defaultAlbumsSortingRule,
    ) = navigationDataSource.flatMapLatest {
        it.albums(sortingRule)
    }

    /**
     * @see MediaDataSource.artists
     */
    fun artists(
        sortingRule: SortingRule = defaultArtistsSortingRule,
    ) = navigationDataSource.flatMapLatest { it.artists(sortingRule) }

    /**
     * @see MediaDataSource.genres
     */
    fun genres(
        sortingRule: SortingRule = defaultGenresSortingRule,
    ) = navigationDataSource.flatMapLatest { it.genres(sortingRule) }

    /**
     * @see MediaDataSource.playlists
     */
    fun playlists(
        sortingRule: SortingRule = defaultPlaylistsSortingRule,
    ) = navigationDataSource.flatMapLatest { it.playlists(sortingRule) }

    /**
     * @see MediaDataSource.search
     */
    fun search(query: String) = navigationDataSource.flatMapLatest { it.search(query) }

    /**
     * @see MediaDataSource.audio
     */
    fun audio(audioUri: Uri) = withMediaItemsDataSourceFlow(audioUri) {
        audio(audioUri)
    }

    /**
     * @see MediaDataSource.album
     */
    fun album(albumUri: Uri) = withMediaItemsDataSourceFlow(albumUri) {
        album(albumUri)
    }

    /**
     * @see MediaDataSource.artist
     */
    fun artist(artistUri: Uri) = withMediaItemsDataSourceFlow(artistUri) {
        artist(artistUri)
    }

    /**
     * @see MediaDataSource.genre
     */
    fun genre(genreUri: Uri) = withMediaItemsDataSourceFlow(genreUri) {
        genre(genreUri)
    }

    /**
     * @see MediaDataSource.playlist
     */
    fun playlist(playlistUri: Uri) = withMediaItemsDataSourceFlow(playlistUri) {
        playlist(playlistUri)
    }

    /**
     * @see MediaDataSource.audioPlaylistsStatus
     */
    fun audioPlaylistsStatus(audioUri: Uri) = withMediaItemsDataSourceFlow(audioUri) {
        audioPlaylistsStatus(audioUri)
    }

    /**
     * @see MediaDataSource.createPlaylist
     */
    suspend fun createPlaylist(
        provider: Provider, name: String
    ) = getDataSource(provider)?.createPlaylist(
        name
    ) ?: RequestStatus.Error(
        MediaError.NOT_FOUND
    )

    /**
     * @see MediaDataSource.renamePlaylist
     */
    suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        withMediaItemsDataSource(playlistUri) {
            renamePlaylist(playlistUri, name)
        }

    /**
     * @see MediaDataSource.deletePlaylist
     */
    suspend fun deletePlaylist(playlistUri: Uri) = withMediaItemsDataSource(playlistUri) {
        deletePlaylist(playlistUri)
    }

    /**
     * @see MediaDataSource.addAudioToPlaylist
     */
    suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri) =
        withMediaItemsDataSource(playlistUri, audioUri) {
            addAudioToPlaylist(playlistUri, audioUri)
        }

    /**
     * @see MediaDataSource.removeAudioFromPlaylist
     */
    suspend fun removeAudioFromPlaylist(playlistUri: Uri, audioUri: Uri) =
        withMediaItemsDataSource(playlistUri, audioUri) {
            removeAudioFromPlaylist(playlistUri, audioUri)
        }

    /**
     * Get the [MediaDataSource] associated with the given [Provider].
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     * @return The corresponding [MediaDataSource]
     */
    private fun getDataSource(
        providerType: ProviderType, providerTypeId: Long
    ) = allProvidersToDataSource.value.firstOrNull { (provider, _) ->
        providerType == provider.type && providerTypeId == provider.typeId
    }?.second

    /**
     * Get the [MediaDataSource] associated with the given [Provider].
     *
     * @param provider The provider
     * @return The corresponding [MediaDataSource]
     */
    private fun getDataSource(provider: Provider) = getDataSource(provider.type, provider.typeId)

    /**
     * Find the [MediaDataSource] that handles the given URIs and call the given predicate on it.
     *
     * @param uris The URIs to check
     * @param predicate The predicate to call on the [MediaDataSource]
     * @return A flow containing the result of the predicate. It will emit a not found error if
     *   no [MediaDataSource] can handle the given URIs
     */
    private fun <T> withMediaItemsDataSourceFlow(
        vararg uris: Uri, predicate: MediaDataSource.() -> Flow<RequestStatus<T, MediaError>>
    ) = allProvidersToDataSource.flatMapLatest {
        it.firstOrNull { (_, dataSource) ->
            uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
        }?.second?.predicate() ?: flowOf(RequestStatus.Error(MediaError.NOT_FOUND))
    }

    /**
     * Find the [MediaDataSource] that handles the given URIs and call the given predicate on it.
     *
     * @param uris The URIs to check
     * @param predicate The predicate to call on the [MediaDataSource]
     * @return A [RequestStatus] containing the result of the predicate. It will return a not found
     *   error if no [MediaDataSource] can handle the given URIs
     */
    private suspend fun <T> withMediaItemsDataSource(
        vararg uris: Uri, predicate: suspend MediaDataSource.() -> RequestStatus<T, MediaError>
    ) = allProvidersToDataSource.value.firstOrNull { (_, dataSource) ->
        uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
    }?.second?.predicate() ?: RequestStatus.Error(MediaError.NOT_FOUND)

    companion object {
        private const val LOCAL_PROVIDER_ID = 0L

        val defaultAlbumsSortingRule = SortingRule(
            SortingStrategy.CREATION_DATE, true
        )

        val defaultArtistsSortingRule = SortingRule(
            SortingStrategy.MODIFICATION_DATE, true
        )

        val defaultGenresSortingRule = SortingRule(
            SortingStrategy.NAME
        )

        val defaultPlaylistsSortingRule = SortingRule(
            SortingStrategy.MODIFICATION_DATE, true
        )
    }
}
