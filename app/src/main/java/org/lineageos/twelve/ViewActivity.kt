/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.datasources.MediaType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.IntentsViewModel
import org.lineageos.twelve.viewmodels.LocalPlayerViewModel

/**
 * An activity used to handle view intents.
 */
class ViewActivity : AppCompatActivity(R.layout.activity_view) {
    // View models
    private val intentsViewModel by viewModels<IntentsViewModel>()
    private val localPlayerViewModel by viewModels<LocalPlayerViewModel>()

    // Views
    private val albumTitleTextView by lazy { findViewById<TextView>(R.id.albumTitleTextView) }
    private val artistNameTextView by lazy { findViewById<TextView>(R.id.artistNameTextView) }
    private val audioTitleTextView by lazy { findViewById<TextView>(R.id.audioTitleTextView) }
    private val dummyThumbnailImageView by lazy { findViewById<ImageView>(R.id.dummyThumbnailImageView) }
    private val playPauseMaterialButton by lazy { findViewById<MaterialButton>(R.id.playPauseMaterialButton) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    // Intents
    private val intentListener = Consumer<Intent> { intentsViewModel.onIntent(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playPauseMaterialButton.setOnClickListener {
            localPlayerViewModel.togglePlayPause()
        }

        intentsViewModel.onIntent(intent)
        addOnNewIntentListener(intentListener)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        coroutineScope {
            launch {
                localPlayerViewModel.mediaMetadata.collectLatest { mediaMetadata ->
                    mediaMetadata.albumTitle?.also {
                        albumTitleTextView.text = it
                        albumTitleTextView.isVisible = true
                    } ?: run {
                        albumTitleTextView.isVisible = false
                    }

                    mediaMetadata.artist?.also {
                        artistNameTextView.text = it
                        artistNameTextView.isVisible = true
                    } ?: run {
                        artistNameTextView.isVisible = false
                    }

                    mediaMetadata.title?.also {
                        audioTitleTextView.text = it
                        audioTitleTextView.isVisible = true
                    } ?: run {
                        audioTitleTextView.isVisible = false
                    }
                }
            }

            launch {
                localPlayerViewModel.isPlaying.collectLatest { isPlaying ->
                    playPauseMaterialButton.setIconResource(
                        when (isPlaying) {
                            true -> R.drawable.ic_pause
                            false -> R.drawable.ic_play_arrow
                        }
                    )
                }
            }

            launch {
                localPlayerViewModel.mediaArtwork.collectLatest {
                    when (it) {
                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            val data = it.data

                            data?.bitmap?.let { bitmap ->
                                thumbnailImageView.load(bitmap)
                                thumbnailImageView.isVisible = true
                                dummyThumbnailImageView.isVisible = false
                            } ?: data?.uri?.let { uri ->
                                thumbnailImageView.load(uri)
                                thumbnailImageView.isVisible = true
                                dummyThumbnailImageView.isVisible = false
                            } ?: run {
                                thumbnailImageView.isVisible = false
                                dummyThumbnailImageView.isVisible = true
                            }
                        }

                        is RequestStatus.Error -> {
                            Log.e(LOG_TAG, "Failed to load artwork")
                            dummyThumbnailImageView.isVisible = true
                            thumbnailImageView.isVisible = false
                        }
                    }
                }
            }

            launch {
                intentsViewModel.parsedIntent.collectLatest { parsedIntent ->
                    parsedIntent?.handle {
                        if (it.action != IntentsViewModel.ParsedIntent.Action.VIEW) {
                            Log.e(LOG_TAG, "Cannot handle action ${it.action}")
                            finish()
                            return@handle
                        }

                        if (it.contents.isEmpty()) {
                            Log.e(LOG_TAG, "No content to play")
                            finish()
                            return@handle
                        }

                        val contentType = it.contents.first().type
                        if (contentType != MediaType.AUDIO) {
                            Log.e(LOG_TAG, "Cannot handle content type $contentType")
                            finish()
                            return@handle
                        }

                        if (it.contents.any { content -> content.type != contentType }) {
                            Log.e(LOG_TAG, "All contents must have the same type")
                            finish()
                            return@handle
                        }

                        localPlayerViewModel.setMediaUris(
                            it.contents.map { content -> content.uri }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ViewActivity::class.simpleName!!
    }
}
