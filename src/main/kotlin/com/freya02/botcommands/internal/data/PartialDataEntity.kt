package com.freya02.botcommands.internal.data

import com.freya02.botcommands.internal.data.adapters.TLongArrayListAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gnu.trove.list.array.TLongArrayList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal open class PartialDataEntity protected constructor(
    val data: String,
    val lifetimeType: LifetimeType,
    val expirationTimestamp: Instant?,
    val timeoutHandlerId: String
) {
    inline fun <reified R> decodeData(gson: Gson = defaultGson): R {
        return gson.fromJson(data, R::class.java)
    }

    companion object {
        val defaultGson: Gson = GsonBuilder()
            .disableJdkUnsafe()
            .registerTypeAdapter(TLongArrayList::class.java, TLongArrayListAdapter)
            .create()

        //TODO this needs to be refactored to use timeout objects, one cannot be null without the other
        fun ofEphemeral(data: Any, timeoutAfter: Duration?, timeoutHandlerId: String) =
            ofType(LifetimeType.EPHEMERAL, data, timeoutAfter, timeoutHandlerId)

        fun ofPersistent(data: Any, timeoutAfter: Duration?, timeoutHandlerId: String) =
            ofType(LifetimeType.PERSISTENT, data, timeoutAfter, timeoutHandlerId)

        private fun ofType(lifetimeType: LifetimeType, data: Any, timeoutAfter: Duration?, timeoutHandlerId: String) =
            PartialDataEntity(
                defaultGson.toJson(data),
                lifetimeType,
                timeoutAfter?.let { Clock.System.now().plus(timeoutAfter) },
                timeoutHandlerId
            )
    }
}
