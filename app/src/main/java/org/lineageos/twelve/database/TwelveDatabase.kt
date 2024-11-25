/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.lineageos.twelve.database.converters.UriConverter
import org.lineageos.twelve.database.dao.ItemDao
import org.lineageos.twelve.database.dao.JellyfinProviderDao
import org.lineageos.twelve.database.dao.PlaylistDao
import org.lineageos.twelve.database.dao.PlaylistItemCrossRefDao
import org.lineageos.twelve.database.dao.PlaylistWithItemsDao
import org.lineageos.twelve.database.dao.ResumptionPlaylistDao
import org.lineageos.twelve.database.dao.SubsonicProviderDao
import org.lineageos.twelve.database.entities.Item
import org.lineageos.twelve.database.entities.JellyfinProvider
import org.lineageos.twelve.database.entities.Playlist
import org.lineageos.twelve.database.entities.PlaylistItemCrossRef
import org.lineageos.twelve.database.entities.ResumptionItem
import org.lineageos.twelve.database.entities.ResumptionPlaylist
import org.lineageos.twelve.database.entities.SubsonicProvider

@Database(
    entities = [
        /* Playlist */
        Playlist::class,
        Item::class,
        PlaylistItemCrossRef::class,

        /* Resumption */
        ResumptionItem::class,
        ResumptionPlaylist::class,

        /* Providers */
        JellyfinProvider::class,
        SubsonicProvider::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@TypeConverters(UriConverter::class)
abstract class TwelveDatabase : RoomDatabase() {
    abstract fun getItemDao(): ItemDao
    abstract fun getJellyfinProviderDao(): JellyfinProviderDao
    abstract fun getPlaylistDao(): PlaylistDao
    abstract fun getPlaylistItemCrossRefDao(): PlaylistItemCrossRefDao
    abstract fun getPlaylistWithItemsDao(): PlaylistWithItemsDao
    abstract fun getResumptionPlaylistDao(): ResumptionPlaylistDao
    abstract fun getSubsonicProviderDao(): SubsonicProviderDao

    companion object {
        @Volatile
        private var INSTANCE: TwelveDatabase? = null

        // Singleton to get the instance of the database
        fun getInstance(context: Context): TwelveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TwelveDatabase::class.java,
                    "twelve_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
