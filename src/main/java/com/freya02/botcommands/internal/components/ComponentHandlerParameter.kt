package com.freya02.botcommands.internal.components

import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KParameter

class ComponentHandlerParameter(
    parameter: KParameter,
    index: Int
) : CommandParameter<ComponentParameterResolver>(
    ComponentParameterResolver::class, parameter, index
) {
    override fun optionAnnotations() = listOf(AppOption::class)
}