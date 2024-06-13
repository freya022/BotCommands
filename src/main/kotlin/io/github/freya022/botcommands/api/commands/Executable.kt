package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.parameters.IAggregatedParameter
import kotlin.reflect.KFunction

interface Executable {
    val function: KFunction<*>
    val parameters: List<IAggregatedParameter>
}