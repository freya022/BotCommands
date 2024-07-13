package io.github.freya022.botcommands.internal.utils

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import io.github.freya022.botcommands.api.core.utils.runIgnoringResponse
import io.github.freya022.botcommands.internal.core.exceptions.InternalException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import java.lang.reflect.InvocationTargetException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KFunction
import kotlin.time.Duration.Companion.seconds

internal fun throwInternal(message: String): Nothing =
    throw InternalException(message)

internal fun throwInternal(function: KFunction<*>, message: String): Nothing =
    throw InternalException("$message\n    Function: ${function.shortSignature}")

internal fun throwInternal(message: String, declarationSite: DeclarationSite? = null): Nothing =
    when (declarationSite) {
        null -> throw InternalException(message)
        else -> throw InternalException("$message\n    Declared at: $declarationSite")
    }

internal fun throwArgument(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("$message\n    Function: ${function.shortSignature}")

internal fun Throwable.rethrow(message: String): Nothing =
    throw RuntimeException(message, this)

internal fun Throwable.rethrowAt(message: String, function: KFunction<*>): Nothing =
    throw RuntimeException("$message\n    Function: ${function.shortSignature}", this)

internal fun Throwable.rethrowAt(message: String, declarationSite: DeclarationSite): Nothing =
    throw RuntimeException("$message\n    Declared at: $declarationSite", this)

internal fun Throwable.rethrowAt(exceptionSupplier: (String, Throwable) -> Throwable, message: String, declarationSite: DeclarationSite): Nothing =
    throw exceptionSupplier("$message\n    Declared at: $declarationSite", this)

internal fun throwArgument(message: String, declarationSite: DeclarationSite? = null): Nothing =
    when (declarationSite) {
        null -> throw IllegalArgumentException(message)
        else -> throw IllegalArgumentException("$message\n    Declared at: $declarationSite")
    }

internal fun throwState(message: String, declarationSite: DeclarationSite? = null): Nothing =
    when (declarationSite) {
        null -> throw IllegalStateException(message)
        else -> throw IllegalStateException("$message\n    Declared at: $declarationSite")
    }

internal fun throwState(message: String, function: KFunction<*>): Nothing =
    throw IllegalStateException("$message\n    Function: ${function.shortSignature}")

internal fun throwState(message: String): Nothing =
    throw IllegalStateException(message)

@OptIn(ExperimentalContracts::class)
internal inline fun requireAt(value: Boolean, function: KFunction<*>? = null, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        if (function != null)
            throwArgument(function, lazyMessage())
        else
            throwArgument(lazyMessage())
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun requireAt(value: Boolean, declarationSite: DeclarationSite? = null, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwArgument(lazyMessage(), declarationSite)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun checkAt(value: Boolean, declarationSite: DeclarationSite? = null, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwState(lazyMessage(), declarationSite)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun checkAt(value: Boolean, function: KFunction<*>? = null, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        if (function != null)
            throwState(lazyMessage(), function)
        else
            throwState(lazyMessage())
    }
}

internal fun Throwable.unwrap(): Throwable {
    if (this is InvocationTargetException) return targetException
    return this
}

internal suspend fun IReplyCallback.replyExceptionMessage(
    message: String
) = runIgnoringResponse(ErrorResponse.UNKNOWN_INTERACTION, ErrorResponse.UNKNOWN_WEBHOOK) {
    if (isAcknowledged) {
        // Give ourselves 5 seconds to delete
        if (Instant.fromEpochMilliseconds(hook.expirationTimestamp) - 5.seconds > Clock.System.now())
            hook.sendMessage(message)
                .setEphemeral(true)
                .deleteDelayed(5.seconds)
                .await()
    } else {
        reply(message)
            .setEphemeral(true)
            .await()
    }
}