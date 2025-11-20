@file:OptIn(ExperimentalContracts::class)

package dev.freya02.botcommands.typesafe.messages.internal.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal inline fun require(value: Boolean, exceptionFactory: (String) -> Throwable, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throw exceptionFactory(lazyMessage())
    }
}
