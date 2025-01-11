/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.selectItem
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.utils.PickPlaylistContract
import org.lineageos.twelve.viewmodels.CreateOrImportPlaylistViewModel

class CreateOrImportPlaylistDialogFragment : MaterialDialogFragment(
    R.layout.fragment_create_or_import_playlist_dialog
) {
    // View models
    private val viewModel by viewModels<CreateOrImportPlaylistViewModel>()

    // Views
    private val createOrImportPlaylistImageView by getViewProperty<ImageView>(R.id.createOrImportPlaylistImageView)
    private val createOrImportPlaylistTextView by getViewProperty<TextView>(R.id.createOrImportPlaylistTextView)
    private val cancelMaterialButton by getViewProperty<MaterialButton>(R.id.cancelMaterialButton)
    private val createMaterialButton by getViewProperty<MaterialButton>(R.id.createMaterialButton)
    private val importMaterialButton by getViewProperty<MaterialButton>(R.id.importMaterialButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val playlistNameTextInputLayout by getViewProperty<TextInputLayout>(R.id.playlistNameTextInputLayout)
    private val providerAutoCompleteTextView by getViewProperty<MaterialAutoCompleteTextView>(R.id.providerAutoCompleteTextView)
    private val providerTextInputLayout by getViewProperty<TextInputLayout>(R.id.providerTextInputLayout)

    // Arguments
    private val providerIdentifier: ProviderIdentifier?
        get() = arguments?.getParcelable(ARG_PROVIDER_IDENTIFIER, ProviderIdentifier::class)
    private val allowImport: Boolean
        get() = arguments?.getBoolean(ARG_ALLOW_IMPORT, true) ?: true

    // Activity callbacks
    private val getPlaylistFile = registerForActivityResult(PickPlaylistContract()) { output ->
        output?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val inputStream = requireContext().contentResolver.openInputStream(output.uri)
                fullscreenLoadingProgressBar.withProgress {
                    inputStream?.use { stream ->
                        viewModel.importPlaylist(output.name, stream)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setProviderIdentifier(providerIdentifier)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providerAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setProviderPosition(position)
        }

        playlistNameTextInputLayout.editText!!.apply {
            setText(viewModel.getPlaylistName())
            doOnTextChanged { text, _, _, _ ->
                playlistNameTextInputLayout.error = null
                viewModel.setPlaylistName(text?.toString() ?: "")
            }
        }

        cancelMaterialButton.setOnClickListener {
            findNavController().navigateUp()
        }

        createMaterialButton.setOnClickListener {
            if (viewModel.isPlaylistNameEmpty()) {
                playlistNameTextInputLayout.error = getString(
                    R.string.create_playlist_error_empty_name
                )
                return@setOnClickListener
            }

            playlistNameTextInputLayout.error = null

            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    viewModel.createPlaylist()
                    findNavController().navigateUp()
                }
            }
        }

        importMaterialButton.setOnClickListener {
            getPlaylistFile.launch(
                PickPlaylistContract.createInput(
                    PickPlaylistContract.PLAYLIST_MIME_TYPES,
                    viewModel.getPlaylistName(),
                )
            )
        }

        when (allowImport) {
            true -> {
                createOrImportPlaylistImageView.contentDescription =
                    getString(R.string.create_or_import_playlist)
                createOrImportPlaylistTextView.text = getString(R.string.create_or_import_playlist)
                importMaterialButton.isVisible = true
            }

            false -> {
                createOrImportPlaylistImageView.contentDescription =
                    getString(R.string.create_playlist)
                createOrImportPlaylistTextView.text = getString(R.string.create_playlist)
                importMaterialButton.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()
            }
        }
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.providersWithSelection.collectLatest { providersWithSelection ->
                val (providers, position) = providersWithSelection

                providerAutoCompleteTextView.setSimpleItems(
                    providers.map { provider ->
                        getString(
                            R.string.provider_format,
                            provider.name,
                            getString(provider.type.nameStringResId),
                        )
                    }.toTypedArray()
                )

                position?.also {
                    val provider = providers[it]

                    // Only allow importing to the local provider
                    importMaterialButton.isVisible = importMaterialButton.isVisible &&
                            provider.type == ProviderType.LOCAL

                    providerAutoCompleteTextView.selectItem(it)
                    providerTextInputLayout.setStartIconDrawable(
                        provider.type.iconDrawableResId
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_PROVIDER_IDENTIFIER = "provider_identifier"
        private const val ARG_ALLOW_IMPORT = "allow_import"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param providerIdentifier A [ProviderIdentifier] to pre-fill the provider field
         * @param allowImport Whether to allow importing playlists
         */
        fun createBundle(
            providerIdentifier: ProviderIdentifier? = null,
            allowImport: Boolean = true,
        ) = bundleOf(
            ARG_PROVIDER_IDENTIFIER to providerIdentifier,
            ARG_ALLOW_IMPORT to allowImport,
        )
    }
}
