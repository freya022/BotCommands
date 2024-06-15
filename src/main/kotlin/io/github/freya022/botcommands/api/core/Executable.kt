package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import kotlin.reflect.KFunction

interface Executable {
    val function: KFunction<*>
    val parameters: List<AggregatedParameter>
}