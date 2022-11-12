package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.data.adapters.TLongArrayListAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gnu.trove.list.array.TLongArrayList

internal open class PartialDataEntity protected constructor(
    val data: String,
    internal val _dataType: String, // Transformed into an Enum when requested
    val lifetimeType: LifetimeType,
    val expiration: DataEntityExpiration?
) {
    inline fun <reified E : Enum<E>> getDataType(): E {
        return enumValueOf(_dataType)
    }

    inline fun <reified R : SerializableDataEntity> decodeData(gson: Gson = defaultGson): R {
        return gson.fromJson(data, R::class.java)
    }

    companion object {
        val defaultGson: Gson = GsonBuilder()
            .registerTypeAdapter(TLongArrayList::class.java, TLongArrayListAdapter)
            .create()

        fun ofEphemeral(data: SerializableDataEntity, timeout: DataEntityTimeout?) =
            ofType(LifetimeType.EPHEMERAL, data, timeout)

        fun ofPersistent(data: SerializableDataEntity, timeout: DataEntityTimeout?) =
            ofType(LifetimeType.PERSISTENT, data, timeout)

        private fun ofType(lifetimeType: LifetimeType, data: SerializableDataEntity, timeout: DataEntityTimeout?) =
            PartialDataEntity(
                defaultGson.toJson(data),
                data.type.name,
                lifetimeType,
                timeout?.toDateEntityExpiration()
            )
    }
}
