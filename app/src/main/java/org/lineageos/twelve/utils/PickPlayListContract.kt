/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickPlayListContract : ActivityResultContract<
        PickPlayListContract.PickPlayListContractInput,
        PickPlayListContract.PickPlayListContractOutput?>() {
    class PickPlayListContractInput(val mimeTypes: Array<String>, val name: String)
    class PickPlayListContractOutput(val uri: Uri, val name: String)

    lateinit var name: String

    override fun createIntent(context: Context, input: PickPlayListContractInput) =
        Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, input.mimeTypes)
            .also {
                name = input.name
            }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            PickPlayListContractOutput(it.data!!, name)
        }

    companion object {
        const val EXTRA_PLAYLIST_NAME = "playlist_name"

        fun createInput(mimeTypes: Array<String>, name: String) =
            PickPlayListContractInput(mimeTypes, name)
    }
}
