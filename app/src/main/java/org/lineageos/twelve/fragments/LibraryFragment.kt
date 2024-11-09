/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty

/**
 * Music library.
 */
class LibraryFragment : Fragment(R.layout.fragment_library) {
    // Views
    private val tabLayout by getViewProperty<TabLayout>(R.id.tabLayout)

    // ViewPager2
    private enum class Menus(
        @StringRes val titleStringResId: Int,
        @DrawableRes val iconDrawableResId: Int,
        @IdRes val navigationId: Int,
    ) {
        ALBUMS(
            R.string.library_fragment_menu_albums,
            R.drawable.ic_album,
            R.id.action_libraryFragment_to_fragment_albums,
        ),
        ARTISTS(
            R.string.library_fragment_menu_artists,
            R.drawable.ic_person,
            R.id.action_libraryFragment_to_fragment_artists,
        ),
        GENRES(
            R.string.library_fragment_menu_genres,
            R.drawable.ic_genres,
            R.id.action_libraryFragment_to_fragment_genres,
        ),
        PLAYLISTS(
            R.string.library_fragment_menu_playlists,
            R.drawable.ic_playlist_play,
            R.id.action_libraryFragment_to_fragment_playlists,
        ),
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Menus.entries.forEach {
            tabLayout.addTab(
                tabLayout.newTab().apply {
                    setText(it.titleStringResId)
                    setContentDescription(it.titleStringResId)
                    setIcon(it.iconDrawableResId)
                    setTag(it.navigationId)
                }
            )
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == null) return

                findNavController().navigate(tab.tag as Int)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }
}
