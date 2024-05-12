package io.github.freya022.botcommands.api.components.builder

import dev.minn.jda.ktx.util.ref
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.api.components.data.ITimeoutData
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import javax.annotation.CheckReturnValue
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.time.*
import java.time.Duration as JavaDuration

/**
 * Allows components to have timeouts.
 *
 * The component will be deleted from the database on expiration.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 */
interface ITimeoutableComponent<T : ITimeoutableComponent<T>> : BuilderInstanceHolder<T> {
    val expiresAt: Instant?
    val timeout: ComponentTimeout?

    /**
     * Removes the timeout from this component.
     */
    @CheckReturnValue
    fun noTimeout(): T

    /**
     * Sets the timeout on this component, invalidating the component on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     */
    @CheckReturnValue
    fun timeout(timeout: Long, timeoutUnit: TimeUnit): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()))

    /**
     * Sets the timeout on this component, invalidating the component on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The duration of the timeout
     */
    @CheckReturnValue
    fun timeout(timeout: JavaDuration): T =
        timeout(timeout.toKotlinDuration())

    /**
     * Sets the timeout on this component, invalidating the component on expiration.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * @param timeout The duration of the timeout
     */
    @JvmSynthetic
    fun timeout(timeout: Duration): T
}

/**
 * Allows components to have persistent timeouts.
 *
 * These timeouts are represented by a method with a [ComponentTimeoutHandler] or [GroupTimeoutHandler] annotation on it,
 * and will still exist (and be rescheduled) after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IPersistentTimeoutableComponent<T : IPersistentTimeoutableComponent<T>> : ITimeoutableComponent<T> {
    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the timeout handler with the given name and its arguments.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [@ComponentTimeoutHandler][ComponentTimeoutHandler] or [@GroupTimeoutHandler][GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     *
     * @see ComponentTimeoutHandler @ComponentTimeoutHandler
     * @see GroupTimeoutHandler @GroupTimeoutHandler
     */
    @CheckReturnValue
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg data: Any?): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *data)

    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the timeout handler with the given name and its arguments.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [@ComponentTimeoutHandler][ComponentTimeoutHandler] or [@GroupTimeoutHandler][GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     *
     * @see ComponentTimeoutHandler @ComponentTimeoutHandler
     * @see GroupTimeoutHandler @GroupTimeoutHandler
     */
    @CheckReturnValue
    fun timeout(timeout: JavaDuration, handlerName: String, vararg data: Any?): T =
        timeout(timeout.toKotlinDuration(), handlerName, *data)

    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the timeout handler with the given name and its arguments.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Timeout data
     * The data passed is transformed with [toString][Object.toString],
     * except [snowflakes][ISnowflake] which get their IDs stored.
     *
     * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
     *
     * @param timeout The duration of the timeout
     * @param handlerName The name of the handler to run when the button is clicked,
     * defined by either [@ComponentTimeoutHandler][ComponentTimeoutHandler] or [@GroupTimeoutHandler][GroupTimeoutHandler] depending on the type
     * @param data The data to pass to the component handler
     *
     * @see ComponentTimeoutHandler @ComponentTimeoutHandler
     * @see GroupTimeoutHandler @GroupTimeoutHandler
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg data: Any?): T
}

/**
 * Allows components to have ephemeral timeouts.
 *
 * These timeouts will not exist anymore after a restart.
 *
 * @see ITimeoutableComponent
 */
interface IEphemeralTimeoutableComponent<T : IEphemeralTimeoutableComponent<T>> : ITimeoutableComponent<T> {
    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the given timeout handler.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param timeout The duration before timeout
     * @param handler The handler to run when the button is clicked
     */
    @CheckReturnValue
    fun timeout(timeout: JavaDuration, handler: Runnable): T =
        timeout(timeout.toKotlinDuration()) { handler.run() }

    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the given timeout handler.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * @param timeout The value of the timeout
     * @param timeoutUnit The unit of the timeout
     * @param handler The handler to run when the button is clicked
     */
    @CheckReturnValue
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    /**
     * Sets the timeout on this component, invalidating the component on expiration,
     * and running the given timeout handler.
     *
     * **Note:** Components inside groups cannot have timeouts.
     *
     * ### Timeout cancellation
     * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
     *
     * ### Component deletion
     * - If the component is a group, then all of its owned components will also be deleted.
     * - If the component is inside a group, then all the group's components will also be deleted.
     *
     * ### Captured entities
     * Pay *extra* attention to not capture JDA entities in such handlers
     * as [they can stop being updated by JDA](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected).
     *
     * You can still use [User.ref] and such from JDA-KTX to attenuate this issue,
     * even though it will return you an outdated object if the entity cannot be found anymore.
     *
     * @param timeout The duration of the timeout
     * @param handler The handler to run when the button is clicked
     */
    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): T
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData> T.timeout(duration: Duration, func: suspend (event: E) -> Unit): T {
    return bindToCallable(duration, func as KFunction<*>, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData> T.timeout(duration: Duration, func: (event: E) -> Unit): T {
    return bindToCallable(duration, func as KFunction<*>, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1> T.timeout(duration: Duration, func: suspend (event: E, T1) -> Unit, arg1: T1): T {
    return bindToCallable(duration, func as KFunction<*>, listOf<Any?>(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1> T.timeout(duration: Duration, func: (event: E, T1) -> Unit, arg1: T1): T {
    return bindToCallable(duration, func as KFunction<*>, listOf<Any?>(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2> T.timeout(duration: Duration, func: suspend (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2> T.timeout(duration: Duration, func: (event: E, T1, T2) -> Unit, arg1: T1, arg2: T2): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3> T.timeout(duration: Duration, func: (event: E, T1, T2, T3) -> Unit, arg1: T1, arg2: T2, arg3: T3): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5, T6) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5, T6, T7) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.timeout(duration: Duration, func: suspend (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted, like with [IUniqueComponent.oneUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 *
 * ### Timeout data
 * The data passed is transformed with [toString][Object.toString],
 * except [snowflakes][ISnowflake] which get their IDs stored.
 *
 * The data can only be reconstructed if a [TimeoutParameterResolver] exists for the handler's parameter type.
 */
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.timeout(duration: Duration, func: (event: E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> Unit, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): T {
    return bindToCallable(duration, func as KFunction<*>, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

private fun <T : IPersistentTimeoutableComponent<T>> T.bindToCallable(duration: Duration, func: KFunction<*>, data: List<Any?>): T {
    return timeout(duration, findHandlerName(func), *data.toTypedArray())
}

private fun findHandlerName(func: KFunction<*>): String {
    val componentTimeoutName = func.findAnnotation<ComponentTimeoutHandler>()?.name
    val groupTimeoutName = func.findAnnotation<GroupTimeoutHandler>()?.name

    if (componentTimeoutName != null && groupTimeoutName != null)
        throwUser(func, "Cannot have the same function with the two annotation")
    else if (componentTimeoutName != null)
        return componentTimeoutName
    else if (groupTimeoutName != null)
        return groupTimeoutName

    throwUser(func, "Could not find ${annotationRef<ComponentTimeoutHandler>()} or ${annotationRef<GroupTimeoutHandler>()}")
}