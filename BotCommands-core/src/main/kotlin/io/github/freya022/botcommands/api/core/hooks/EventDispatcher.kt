package io.github.freya022.botcommands.api.core.hooks

import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.config.BCoroutineScopesConfig
import io.github.freya022.botcommands.api.core.events.BEvent
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Dispatches JDA and BC events to [@BEventListener][BEventListener] methods.
 */
@InterfacedService(acceptMultiple = false)
abstract class EventDispatcher internal constructor() {

    /**
     * Registers the given [listener] for JDA and BC events.
     *
     * Only methods annotated by [@BEventListener][BEventListener] will be registered,
     * use your `JDA` or `ShardManager` instance to register event listeners that use
     * [CoroutineEventListener], [ListenerAdapter] or [EventListener].
     */
    abstract fun addEventListener(listener: Any)

    /**
     * Unregisters the [listener] from JDA and BC events.
     *
     * Only methods annotated by [@BEventListener][BEventListener] will be unregistered,
     * use your `JDA` or `ShardManager` instance to unregister event listeners that use
     * [CoroutineEventListener], [ListenerAdapter] or [EventListener].
     */
    abstract fun removeEventListener(listener: Any)

    /**
     * Dispatches the given [event] to the registered listeners.
     *
     * The event must either be a [GenericEvent] or a [BEvent], passing neither of them is a no-op.
     *
     * ### Execution model
     * This method will block until all [BLOCKING][BEventListener.RunMode.BLOCKING] listeners,
     * are fired and have returned.
     *
     * [ASYNC][BEventListener.RunMode.ASYNC] listeners will then be launched on [BCoroutineScopesConfig.eventDispatcherScope],
     * without blocking.
     *
     * Finally, [SHARED][BEventListener.RunMode.SHARED] listeners will be fired on
     * the scope of the [CoroutineEventManager] without blocking,
     * which can be configured with [ICoroutineEventManagerSupplier].
     *
     * Any thrown exception will be logged separately and do not affect other listeners.
     */
    @JvmSynthetic
    abstract suspend fun dispatchEvent(event: Any)

    /**
     * Dispatches the given [event] to the registered listeners.
     *
     * The event must either be a [GenericEvent] or a [BEvent], passing neither of them is a no-op.
     *
     * ### Execution model
     * This method will block until all [BLOCKING][BEventListener.RunMode.BLOCKING] listeners,
     * are fired and have returned.
     *
     * [ASYNC][BEventListener.RunMode.ASYNC] listeners will then be launched on [BCoroutineScopesConfig.eventDispatcherScope],
     * without blocking.
     *
     * Finally, [SHARED][BEventListener.RunMode.SHARED] listeners will be fired on
     * the scope of the [CoroutineEventManager] without blocking,
     * which can be configured with [ICoroutineEventManagerSupplier].
     *
     * Any thrown exception will be logged separately and do not affect other listeners.
     */
    @JvmName("dispatchEvent")
    fun dispatchEventJava(event: Any) = runBlocking { dispatchEvent(event) }

    /**
     * Dispatches the given [event] to the registered listeners.
     *
     * The event must either be a [GenericEvent] or a [BEvent], passing neither of them is a no-op.
     *
     * ### Execution model
     * This method will fire all listeners as if they were [ASYNC][BEventListener.RunMode.ASYNC],
     * and never blocks.
     *
     * Any thrown exception will be logged separately and do not affect other listeners.
     */
    abstract fun dispatchEventAsync(event: Any): List<Deferred<Unit>>
}