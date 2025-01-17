/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.fragments

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.lineageos.twelve.R

abstract class InnerNavigationFragment(@LayoutRes private val contentLayoutId: Int) :
    Fragment(contentLayoutId) {
    protected val parentNavController by lazy {
        requireActivity().findNavController(R.id.navHostFragment)
    }
}
