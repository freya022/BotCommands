package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilder<T, Any>(context, name, function),
    ApplicationOptionRegistry<T> {

    internal abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    /**
     * Set of filters preventing this command from executing.
     *
     * @see ApplicationCommandFilter
     * @see ApplicationCommandRejectionHandler
     */
    val filters: MutableList<ApplicationCommandFilter<*>> = arrayListOf()

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }
}

/**
 * Convenience extension to load an [ApplicationCommandFilter] service.
 *
 * Typically used as `filters += filter<MyApplicationCommandFilter>()`
 */
inline fun <reified T : ApplicationCommandFilter<*>> ApplicationCommandBuilder<*>.filter(): T {
    return context.getService<T>()
}