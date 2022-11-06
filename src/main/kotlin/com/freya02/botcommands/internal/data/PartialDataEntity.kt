package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.data.adapters.TLongArrayListAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gnu.trove.list.array.TLongArrayList

internal open class PartialDataEntity protected constructor(
    val data: String,
    val lifetimeType: LifetimeType,
    val expiration: DataEntityExpiration?
) {
    inline fun <reified R> decodeData(gson: Gson = defaultGson): R {
        return gson.fromJson(data, R::class.java)
    }

    companion object {
        val defaultGson: Gson = GsonBuilder()
            .registerTypeAdapter(TLongArrayList::class.java, TLongArrayListAdapter)
            .create()

        fun ofEphemeral(data: Any, timeout: DataEntityTimeout?) =
            ofType(LifetimeType.EPHEMERAL, data, timeout)

        fun ofPersistent(data: Any, timeout: DataEntityTimeout?) =
            ofType(LifetimeType.PERSISTENT, data, timeout)

        private fun ofType(lifetimeType: LifetimeType, data: Any, timeout: DataEntityTimeout?) =
            PartialDataEntity(
                defaultGson.toJson(data),
                lifetimeType,
                timeout?.toDateEntityExpiration()
            )
    }
}
