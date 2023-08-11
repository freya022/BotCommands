package com.freya02.botcommands.internal.core.exceptions

import com.freya02.botcommands.api.BCInfo
import net.dv8tion.jda.api.JDAInfo

internal class InternalException internal constructor(
    message: String
) : RuntimeException("$message, please report this to the devs. ${getDiagnosticVersions()}")

private fun getDiagnosticVersions() = "[ BC version: ${BCInfo.VERSION} | Current JDA version: ${JDAInfo.VERSION} ]"