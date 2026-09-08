package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.config.BEventManagerConfig
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.hooks.EventDispatcher
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirementsProvider
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.concurrent.TimeUnit

/**
 * Annotates a function as an event listener for a JDA, BC, or custom event.
 *
 * Remember to always check the requirements of the events you're listening to!
 * If an event does not fulfill their requirements, and they are not ignored, then the listener will be disabled!
 *
 * ### Requirements
 * - The declaring class must be a service
 * - The function must not be static
 * - The first argument is typically a subclass of [GenericEvent] or [BGenericEvent], but can be anything other than `Any`/`Object`
 * - If the event being listened to is a custom event, then a [CustomEventRequirementsProvider] service must be available.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BEventListener(
    /**
     * How this event listener should run, see [RunMode] values.
     *
     * @see RunMode
     */
    val mode: RunMode = RunMode.SHARED,
    /**
     * The priority for this event listener
     *
     * **Note:** The run order of listeners is based on the [mode]'s [order][RunMode.order] and then the priority,
     * for example, a listener with the [SHARED][RunMode.SHARED] mode and maximum priority,
     * will still run *after* listeners with the [BLOCKING][RunMode.BLOCKING] mode.
     *
     * In other words, blocking listeners are first, then async listeners, and finally inherited listeners.
     */
    val priority: Int = 0, //Default priority
    /**
     * Whether this event listener should be kept enabled, even if it is missing intents.
     *
     * @see BConfig.ignoredIntents
     */
    val ignoreIntents: Boolean = false,
    /**
     * Intents which should not be required for this event listener.
     */
    val ignoredIntents: Array<GatewayIntent> = [],
    /**
     * The time before the coroutine is canceled,
     * using a non-positive or non-finite value equals to no timeout.
     *
     * **Default:** [BEventManagerConfig.defaultTimeout]
     */
    val timeout: Long = 0,
    /** The time unit used for the timeout */
    val timeoutUnit: TimeUnit = TimeUnit.SECONDS
) {

    /**
     * Represents how the event listener will run
     */
    enum class RunMode(@get:JvmSynthetic internal val order: Int) {

        /**
         * Runs this listener in the coroutine the event was fired from.
         *
         * For JDA events, this will run on [BCoroutineScopesConfig.eventManagerScope],
         * while for other events this will run on the coroutine which dispatched the event.
         *
         * All event listeners will share the same coroutine and run based on their [priority]
         * without blocking the event thread.
         *
         * [BLOCKING] requests always runs before these.
         *
         * Note that this is ignored if the event was dispatched using [EventDispatcher.dispatchEventAsync],
         * instead running all event listeners as [ASYNC].
         */
        SHARED(order = 3),

        /**
         * Runs this listener on the same thread as they were fired in, based on the [priority].
         *
         * For JDA events, this will most likely run on JDA's websocket read thread,
         * blocking it for too long will make the bot unresponsive.
         *
         * These always run first, and should be used **only when necessary**.
         *
         * Note that this is ignored if the event was dispatched using [EventDispatcher.dispatchEventAsync],
         * instead running all event listeners as [ASYNC].
         */
        BLOCKING(order = 1),

        /**
         * Runs this listener in a coroutine from the [event dispatcher scope][BCoroutineScopesConfig.eventDispatcherScope],
         * without blocking the event thread.
         *
         * Each event listener will be launched based on their [priority],
         * and each will have its own coroutine.
         * Remember this does not mean all listeners will run in parallel,
         * as you are limited by how many threads can be used by the scope's dispatcher,
         * or until one of the listeners suspends to let another run.
         *
         * [BLOCKING] requests always runs before these.
         */
        ASYNC(order = 2),
    }
}
