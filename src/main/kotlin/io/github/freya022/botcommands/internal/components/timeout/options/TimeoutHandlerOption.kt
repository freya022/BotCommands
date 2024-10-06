package io.github.freya022.botcommands.internal.components.timeout.options

import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor
import io.github.freya022.botcommands.internal.components.timeout.options.builder.TimeoutHandlerOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal class TimeoutHandlerOption internal constructor(
    override val executable: TimeoutDescriptor<*>,
    optionBuilder: TimeoutHandlerOptionBuilderImpl,
    val resolver: TimeoutParameterResolver<*, *>
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)