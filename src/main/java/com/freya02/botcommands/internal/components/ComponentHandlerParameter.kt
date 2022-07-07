package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KParameter

class ComponentHandlerParameter(
    parameter: KParameter,
    optionBuilder: OptionBuilder,
    val resolver: ComponentParameterResolver
) : CommandParameter(
    parameter, optionBuilder
)