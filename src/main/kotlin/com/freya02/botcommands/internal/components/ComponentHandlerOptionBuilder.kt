package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import kotlin.reflect.KFunction

class ComponentHandlerOptionBuilder(
    owner: KFunction<*>,
    declaredName: String
) : OptionBuilder(owner, declaredName)