package com.freya02.botcommands.core.internal.data

import com.google.gson.Gson
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration

open class PartialDataEntity protected constructor(
    val data: String,
    val lifetimeType: LifetimeType,
    val expirationTimestamp: LocalDateTime?,
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
                timeoutAfter?.let { LocalDateTime.now().plus(timeoutAfter.toJavaDuration()) },
                timeoutHandlerId
            )
    }
}
