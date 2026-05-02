package io.github.freya022.botcommands.internal.core.exceptions

import io.github.freya022.botcommands.api.BCInfo
import net.dv8tion.jda.api.JDAInfo

internal class InternalException @JvmOverloads internal constructor(
    message: String,
    throwable: Throwable? = null,
) : RuntimeException(internalErrorMessage(message), throwable)

fun getDiagnosticVersions() = "[ BC version: ${BCInfo.VERSION} | Current JDA version: ${JDAInfo.VERSION} ]"

fun internalErrorMessage(message: String) = "$message, please report this to the devs. ${getDiagnosticVersions()}"
