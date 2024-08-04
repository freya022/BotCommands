package io.github.freya022.botcommands.api.core.options.builder

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver

interface OptionRegistry<T : OptionAggregateBuilder<T>> : OptionAggregateBuilderContainer<T> {
    /**
     * Declares a service option, allowing injection of services, which must be available.
     *
     * If the service is not available, then either don't declare this command,
     * or make the declaring class disabled by using one of:
     * - [@Condition][Condition]
     * - [@ConditionalService][ConditionalService]
     * - [@Dependencies][Dependencies]
     *
     * @param declaredName Name of the declared parameter which receives the value
     */
    fun serviceOption(declaredName: String)

    /**
     * Declares a custom option, such as an [TextLocalizationContext] (with [@LocalizationBundle][LocalizationBundle]).
     *
     * Additional types can be added by implementing [ICustomResolver].
     *
     * @param declaredName Name of the declared parameter which receives the value
     */
    fun customOption(declaredName: String)
}