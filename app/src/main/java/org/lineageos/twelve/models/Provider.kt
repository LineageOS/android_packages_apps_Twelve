/*
 * SPDX-FileCopyrightText: 2024-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Parcel
import android.os.Parcelable
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.ext.readSerializable

/**
 * A provider instance. The [type] determines how data should be retrieved from the provider.
 * Each provider has an associated [MediaDataSource] and related arguments, but those are not
 * exposed outside of the media repository.
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 * @param name The name of the provider given by the user
 */
class Provider(
    override val type: ProviderType,
    override val typeId: Long,
    val name: String,
) : ProviderIdentifier(type, typeId), UniqueItem<Provider> {
    override fun areItemsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::type,
        Provider::typeId,
    ) == 0

    override fun areContentsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::name,
    ) == 0

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(type)
        dest.writeLong(typeId)
        dest.writeString(name)
    }

    companion object CREATOR : Parcelable.Creator<Provider> {
        override fun createFromParcel(source: Parcel?) = source?.let {
            Provider(
                type = it.readSerializable(ProviderType::class)!!,
                typeId = it.readLong(),
                name = it.readString()!!,
            )
        }

        override fun newArray(size: Int) = arrayOfNulls<Provider>(size)
    }
}
