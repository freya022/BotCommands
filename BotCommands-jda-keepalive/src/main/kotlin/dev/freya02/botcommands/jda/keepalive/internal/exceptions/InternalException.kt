package dev.freya02.botcommands.jda.keepalive.internal.exceptions

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.utils.getSignature
import net.dv8tion.jda.api.JDAInfo
import kotlin.reflect.KFunction

internal class InternalException internal constructor(
    message: String,
    throwable: Throwable? = null,
) : RuntimeException(internalErrorMessage(message), throwable)

internal fun throwInternal(message: String): Nothing =
    throw InternalException(message)

internal fun throwInternal(function: KFunction<*>, message: String): Nothing =
    throw InternalException("$message\n    Function: ${function.getSignature(returnType = true)}")

internal fun getDiagnosticVersions() = "[ BC version: ${BCInfo.VERSION} | Current JDA version: ${JDAInfo.VERSION} ]"

internal fun internalErrorMessage(message: String) = "$message, please report this to the devs. ${getDiagnosticVersions()}"
