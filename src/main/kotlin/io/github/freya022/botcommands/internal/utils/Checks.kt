package io.github.freya022.botcommands.internal.utils

import kotlin.time.Duration

internal object Checks {
    internal fun checkFinite(duration: Duration, name: String) {
        require(duration.isFinite() && duration.isPositive()) {
            "The $name must be finite and positive"
        }
    }

    internal fun checkFitInt(duration: Duration, name: String) {
        require(duration.inWholeMilliseconds in Int.MIN_VALUE..Int.MAX_VALUE) {
            "The $name must be finite and positive"
        }
    }
}