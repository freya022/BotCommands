package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import kotlin.reflect.KFunction

interface Executable {
    val function: KFunction<*>
    val parameters: List<IAggregatedParameter>
}