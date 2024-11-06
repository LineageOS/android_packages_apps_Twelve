/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve

import android.content.ContentResolver
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import org.lineageos.twelve.viewmodels.MiniAudioPlayerViewModel

class MiniAudioPlayerActivity : AppCompatActivity() {
    // View model
    private val viewModel by viewModels<MiniAudioPlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data ?: return finish()

        when (data.scheme) {
            ContentResolver.SCHEME_CONTENT, ContentResolver.SCHEME_FILE -> {
                viewModel.load(data)
            }

            else -> return finish()
        }
    }
}