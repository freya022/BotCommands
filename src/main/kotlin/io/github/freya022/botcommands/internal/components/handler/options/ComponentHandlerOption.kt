package io.github.freya022.botcommands.internal.components.handler.options

import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor
import io.github.freya022.botcommands.internal.components.handler.options.builder.ComponentHandlerOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal class ComponentHandlerOption internal constructor(
    override val executable: ComponentDescriptor,
    optionBuilder: ComponentHandlerOptionBuilderImpl,
    internal val resolver: ComponentParameterResolver<*, *>
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)