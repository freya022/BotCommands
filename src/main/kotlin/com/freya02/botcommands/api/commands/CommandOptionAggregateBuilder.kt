package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import kotlin.reflect.KFunction

abstract class CommandOptionAggregateBuilder(owner: KFunction<*>, declaredName: String) : OptionAggregateBuilder(owner, declaredName)
