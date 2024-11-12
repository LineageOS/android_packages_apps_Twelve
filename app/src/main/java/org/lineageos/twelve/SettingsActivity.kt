/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.Px
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import org.lineageos.twelve.ext.ENABLE_OFFLOAD_KEY
import org.lineageos.twelve.ext.enableOffload
import org.lineageos.twelve.ext.setOffset
import org.lineageos.twelve.viewmodels.SettingsViewModel
import kotlin.reflect.safeCast

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    // View model
    private val viewModel by viewModels<SettingsViewModel>()

    private val appBarLayout by lazy { findViewById<AppBarLayout>(R.id.appBarLayout) }
    private val coordinatorLayout by lazy { findViewById<CoordinatorLayout>(R.id.coordinatorLayout) }
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.toolbar) }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, RootSettingsFragment())
                .commit()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                ENABLE_OFFLOAD_KEY -> {
                    lifecycleScope.launch {
                        viewModel.toggleOffload(sharedPreferences.enableOffload)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    abstract class SettingsFragment(
        @XmlRes private val preferencesResId: Int,
    ) : PreferenceFragmentCompat() {
        private val settingsActivity
            get() = SettingsActivity::class.safeCast(activity)

        @Px
        private var appBarOffset = -1

        private val offsetChangedListener = AppBarLayout.OnOffsetChangedListener { _, i ->
            appBarOffset = -i
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            settingsActivity?.let { settingsActivity ->
                val appBarLayout = settingsActivity.appBarLayout

                if (appBarOffset != -1) {
                    appBarLayout.setOffset(appBarOffset, settingsActivity.coordinatorLayout)
                } else {
                    appBarLayout.setExpanded(true, false)
                }

                appBarLayout.setLiftOnScrollTargetView(listView)

                appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
            }
        }

        override fun onDestroyView() {
            settingsActivity?.appBarLayout?.apply {
                removeOnOffsetChangedListener(offsetChangedListener)

                setLiftOnScrollTargetView(null)
            }

            super.onDestroyView()
        }

        @CallSuper
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(preferencesResId, rootKey)
        }

        @CallSuper
        override fun onCreateRecyclerView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            savedInstanceState: Bundle?
        ) = super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
            isVerticalScrollBarEnabled = false

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                updatePadding(
                    bottom = insets.bottom,
                    left = insets.left,
                    right = insets.right,
                )

                windowInsets
            }
        }
    }

    class RootSettingsFragment : SettingsFragment(R.xml.root_preferences)
}
