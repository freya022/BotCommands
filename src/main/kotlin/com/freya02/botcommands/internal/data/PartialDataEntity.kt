package com.freya02.botcommands.internal.data

import com.google.gson.Gson
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

open class PartialDataEntity protected constructor(
    val data: String,
    val lifetimeType: LifetimeType,
    val expirationTimestamp: Instant?,
    val timeoutHandlerId: String
) {
    inline fun <reified R> decodeData(gson: Gson = defaultGson): R {
        return gson.fromJson(data, R::class.java)
    }

    companion object {
        val defaultGson = Gson()
        fun ofEphemeral(data: String, timeoutAfter: Duration?, timeoutHandlerId: String) =
            PartialDataEntity(
                data,
                LifetimeType.EPHEMERAL,
                timeoutAfter?.let { Clock.System.now().plus(timeoutAfter) },
                timeoutHandlerId
            )
    }
}
