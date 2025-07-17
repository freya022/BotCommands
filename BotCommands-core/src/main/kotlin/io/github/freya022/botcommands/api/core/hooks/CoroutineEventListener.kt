package io.github.freya022.botcommands.api.core.hooks

import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.time.Duration

interface CoroutineEventListener {

    /**
     * The duration for which this listener's code is allowed to run for.
     *
     * A `null`, non-positive or non-finite value will use the [default timeout][io.github.freya022.botcommands.api.core.config.BEventManagerConfig.defaultTimeout].
     *
     * Once the time has elapsed, a [TimeoutCancellationException] will be thrown after the next resume/suspension point.
     */
    val timeout: Duration?

    /**
     * Unregisters this event listener
     */
    fun cancel()

    suspend fun onEvent(event: GenericEvent)
}
