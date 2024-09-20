package io.github.freya022.botcommands.api.components.builder

import dev.minn.jda.ktx.util.ref
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.annotations.getEffectiveName
import io.github.freya022.botcommands.api.components.data.ITimeoutData
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.components.data.timeout.ComponentTimeout
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.javaMethodInternal
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit
import javax.annotation.CheckReturnValue
import kotlin.reflect.*
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
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
 *
 * ### Component deletion
 * - If the component is a group, then all of its owned components will also be deleted.
 * - If the component is inside a group, then all the group's components will also be deleted.
 */
interface ITimeoutableComponent<T : ITimeoutableComponent<T>> : BuilderInstanceHolder<T> {
    val timeoutDuration: Duration?
    val timeout: ComponentTimeout?

    /**
     * When `true`, resets the timeout duration everytime this component is used.
     *
     * Any component inside a group resets the timeout of the entire group.
     */
    var resetTimeoutOnUse: Boolean

    /**
     * When `true`, resets the timeout duration everytime this component is used.
     *
     * Any component inside a group resets the timeout of the entire group.
     */
    fun resetTimeoutOnUse(resetTimeoutOnUse: Boolean): T

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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
     * The timeout will be canceled once a component has been deleted,
     * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData> T.timeout(duration: Duration, func: KSuspendFunction1<E, Unit>): T {
    return timeoutWithBoundCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData> T.timeout(duration: Duration, func: KFunction1<E, Unit>): T {
    return timeoutWithBoundCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1> T.timeout(duration: Duration, func: KSuspendFunction2<E, T1, Unit>, arg1: T1): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1> T.timeout(duration: Duration, func: KFunction2<E, T1, Unit>, arg1: T1): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2> T.timeout(duration: Duration, func: KSuspendFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2> T.timeout(duration: Duration, func: KFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3> T.timeout(duration: Duration, func: KSuspendFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3> T.timeout(duration: Duration, func: KFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4> T.timeout(duration: Duration, func: KSuspendFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4> T.timeout(duration: Duration, func: KFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5> T.timeout(duration: Duration, func: KSuspendFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5> T.timeout(duration: Duration, func: KFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> T.timeout(duration: Duration, func: KSuspendFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> T.timeout(duration: Duration, func: KFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> T.timeout(duration: Duration, func: KSuspendFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> T.timeout(duration: Duration, func: KFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> T.timeout(duration: Duration, func: KSuspendFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> T.timeout(duration: Duration, func: KFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.timeout(duration: Duration, func: KSuspendFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> T.timeout(duration: Duration, func: KFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutSuspend")
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.timeout(duration: Duration, func: KSuspendFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@Deprecated("Replaced with timeoutWith", ReplaceWith("timeoutWith(duration, func, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10)"))
fun <T : IPersistentTimeoutableComponent<T>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> T.timeout(duration: Duration, func: KFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): T {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData> C.timeoutWith(duration: Duration, func: KSuspendFunction1<E, Unit>): C {
    return timeoutWithBoundCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData> C.timeoutWith(duration: Duration, func: KFunction1<E, Unit>): C {
    return timeoutWithBoundCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1> C.timeoutWith(duration: Duration, func: KSuspendFunction2<E, T1, Unit>, arg1: T1): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1> C.timeoutWith(duration: Duration, func: KFunction2<E, T1, Unit>, arg1: T1): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2> C.timeoutWith(duration: Duration, func: KSuspendFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2> C.timeoutWith(duration: Duration, func: KFunction3<E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3> C.timeoutWith(duration: Duration, func: KSuspendFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3> C.timeoutWith(duration: Duration, func: KFunction4<E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4> C.timeoutWith(duration: Duration, func: KSuspendFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4> C.timeoutWith(duration: Duration, func: KFunction5<E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5> C.timeoutWith(duration: Duration, func: KSuspendFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5> C.timeoutWith(duration: Duration, func: KFunction6<E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> C.timeoutWith(duration: Duration, func: KSuspendFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> C.timeoutWith(duration: Duration, func: KFunction7<E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> C.timeoutWith(duration: Duration, func: KSuspendFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> C.timeoutWith(duration: Duration, func: KFunction8<E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> C.timeoutWith(duration: Duration, func: KSuspendFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> C.timeoutWith(duration: Duration, func: KFunction9<E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.timeoutWith(duration: Duration, func: KSuspendFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.timeoutWith(duration: Duration, func: KFunction10<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallableSuspend")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.timeoutWith(duration: Duration, func: KSuspendFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithBoundCallable")
fun <C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.timeoutWith(duration: Duration, func: KFunction11<E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return timeoutWithBoundCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

private fun <C : IPersistentTimeoutableComponent<C>> C.timeoutWithBoundCallable(duration: Duration, func: KFunction<*>, data: List<Any?>): C {
    return timeout(duration, findHandlerName(func), *data.toTypedArray())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData> C.timeoutWith(duration: Duration, func: KSuspendFunction2<T, E, Unit>): C {
    return timeoutWithClassCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData> C.timeoutWith(duration: Duration, func: KFunction2<T, E, Unit>): C {
    return timeoutWithClassCallable(duration, func, emptyList())
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1> C.timeoutWith(duration: Duration, func: KSuspendFunction3<T, E, T1, Unit>, arg1: T1): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1> C.timeoutWith(duration: Duration, func: KFunction3<T, E, T1, Unit>, arg1: T1): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2> C.timeoutWith(duration: Duration, func: KSuspendFunction4<T, E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2> C.timeoutWith(duration: Duration, func: KFunction4<T, E, T1, T2, Unit>, arg1: T1, arg2: T2): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3> C.timeoutWith(duration: Duration, func: KSuspendFunction5<T, E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3> C.timeoutWith(duration: Duration, func: KFunction5<T, E, T1, T2, T3, Unit>, arg1: T1, arg2: T2, arg3: T3): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4> C.timeoutWith(duration: Duration, func: KSuspendFunction6<T, E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4> C.timeoutWith(duration: Duration, func: KFunction6<T, E, T1, T2, T3, T4, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5> C.timeoutWith(duration: Duration, func: KSuspendFunction7<T, E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5> C.timeoutWith(duration: Duration, func: KFunction7<T, E, T1, T2, T3, T4, T5, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> C.timeoutWith(duration: Duration, func: KSuspendFunction8<T, E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6> C.timeoutWith(duration: Duration, func: KFunction8<T, E, T1, T2, T3, T4, T5, T6, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> C.timeoutWith(duration: Duration, func: KSuspendFunction9<T, E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7> C.timeoutWith(duration: Duration, func: KFunction9<T, E, T1, T2, T3, T4, T5, T6, T7, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> C.timeoutWith(duration: Duration, func: KSuspendFunction10<T, E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8> C.timeoutWith(duration: Duration, func: KFunction10<T, E, T1, T2, T3, T4, T5, T6, T7, T8, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.timeoutWith(duration: Duration, func: KSuspendFunction11<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9> C.timeoutWith(duration: Duration, func: KFunction11<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallableSuspend")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.timeoutWith(duration: Duration, func: KSuspendFunction12<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

/**
 * Sets the timeout on this component, invalidating the component on expiration,
 * and running the timeout handler with the given name and its arguments.
 *
 * **Note:** Components inside groups cannot have timeouts.
 *
 * ### Timeout cancellation
 * The timeout will be canceled once a component has been deleted,
 * including if the component was set to a [single use][IUniqueComponent.singleUse].
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
 * Remember the parameters need to be annotated with [@TimeoutData][TimeoutData].
 */
@JvmName("timeoutWithClassCallable")
fun <T : Any, C : IPersistentTimeoutableComponent<C>, E : ITimeoutData, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> C.timeoutWith(duration: Duration, func: KFunction12<T, E, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Unit>, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): C {
    return timeoutWithClassCallable(duration, func, listOf(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
}

private fun <C : IPersistentTimeoutableComponent<C>> C.timeoutWithClassCallable(duration: Duration, func: KFunction<*>, data: List<Any?>): C {
    val paramType = func.javaMethodInternal.parameterTypes[0]
    require(paramType.isSubclassOf<ITimeoutData>()) {
        "The provided function must have a timeout data as its first parameter, found ${paramType.simpleNestedName}"
    }
    return timeout(duration, findHandlerName(func), data)
}

private fun findHandlerName(func: KFunction<*>): String {
    val componentTimeoutAnnotation = func.findAnnotationRecursive<ComponentTimeoutHandler>()
    val groupTimeoutAnnotation = func.findAnnotationRecursive<GroupTimeoutHandler>()

    if (componentTimeoutAnnotation != null && groupTimeoutAnnotation != null)
        throwArgument(func, "Cannot have the same function with the two annotation")
    else if (componentTimeoutAnnotation != null)
        return componentTimeoutAnnotation.getEffectiveName(func)
    else if (groupTimeoutAnnotation != null)
        return groupTimeoutAnnotation.getEffectiveName(func)

    throwArgument(func, "Could not find ${annotationRef<ComponentTimeoutHandler>()} or ${annotationRef<GroupTimeoutHandler>()}")
}