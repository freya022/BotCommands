package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.core.service.getService

interface ApplicationCommandBuilder<T> : ExecutableCommandBuilder<T>,
                                         ApplicationOptionRegistry<T> where T : ApplicationCommandOptionAggregateBuilder<T> {

    //TODO document
    val topLevelBuilder: TopLevelApplicationCommandBuilder<T>

    /**
     * Set of filters preventing this command from executing.
     *
     * @see ApplicationCommandFilter
     * @see ApplicationCommandRejectionHandler
     */
    val filters: MutableList<ApplicationCommandFilter<*>>
}

/**
 * Convenience extension to load an [ApplicationCommandFilter] service.
 *
 * Typically used as `filters += filter<MyApplicationCommandFilter>()`
 */
inline fun <reified T : ApplicationCommandFilter<*>> ApplicationCommandBuilder<*>.filter(): T {
    return context.getService<T>()
}