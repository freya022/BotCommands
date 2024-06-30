package io.github.freya022.botcommands.api.core.annotations

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BEventListener(
    /** Whether this event listener is executed on its own coroutine, using the [event dispatcher scope][BCoroutineScopesConfig.eventDispatcherScope] */
    val async: Boolean = false,
    /**
     * The priority for this event listener
     *
     * **Note:** While the priority is used when dispatching the events, if another handler is async then it will not be awaited before this one is fired.
     *
     * This means that this handler might start running before the previous (async) one has finished running.
     */
    val priority: Int = 0, //Default priority
    /**
     * Whether this event listener should be kept enabled, even if it is missing intents.
     *
     * @see BConfig.ignoredIntents
     */
    val ignoreIntents: Boolean = false,
    /**
     * The time before the coroutine is canceled, using a negative value means no timeout.
     *
     * **Default:** [CoroutineEventManager.timeout] from [ICoroutineEventManagerSupplier]
     */
    val timeout: Long = 0,
    /** The time unit used for the timeout */
    val timeoutUnit: TimeUnit = TimeUnit.SECONDS)
