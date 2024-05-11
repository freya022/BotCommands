package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilder<T, Any>(context, name, function) {
    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    val filters: MutableList<ApplicationCommandFilter<*>> = arrayListOf()

    /**
     * Declares a custom option, such as an [AppLocalizationContext] (with [@LocalizationBundle][LocalizationBundle]).
     *
     * Additional types can be added by implementing [ICustomResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * Declares a generated option, the supplier gets called on each command execution.
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     *
     * @see GeneratedOption @GeneratedOption
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }
}

inline fun <reified T : ApplicationCommandFilter<*>> ApplicationCommandBuilder<*>.filter(): T {
    return context.getService<T>()
}