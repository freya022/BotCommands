package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.config.BCoroutineScopesConfig
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BEventListener(
    /** Whether this event listener is executed on its own coroutine, using the [event dispatcher scope][BCoroutineScopesConfig.eventDispatcherScope] */
    val async: Boolean = false,
    /** The priority for this event listener */
    val priority: Int = 0, //Default priority
    /** The time before the coroutine is cancelled, using a negative value means no timeout */
    val timeout: Long = 0,
    /** The time unit used for the timeout */
    val timeoutUnit: TimeUnit = TimeUnit.SECONDS)
