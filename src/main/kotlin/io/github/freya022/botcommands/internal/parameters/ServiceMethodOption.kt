package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService

class ServiceMethodOption private constructor(
    optionParameter: OptionParameter,
    val lazyService: Lazy<*>
) : OptionImpl(optionParameter, OptionType.SERVICE) {
    internal constructor(
        optionParameter: OptionParameter,
        serviceContainer: ServiceContainer
    ) : this(optionParameter, lazy { serviceContainer.tryGetWrappedService(optionParameter.typeCheckingParameter).getOrThrow() })
}