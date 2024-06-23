package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfoImpl
import io.github.freya022.botcommands.internal.utils.reference

internal abstract class ApplicationCommandInfoImpl internal constructor(
    builder: ApplicationCommandBuilder<*>
) : AbstractCommandInfoImpl(builder),
    ApplicationCommandInfo,
    ExecutableMixin {

    internal val filters: List<ApplicationCommandFilter<*>> = builder.filters.onEach { filter ->
        require(!filter.global) {
            "Global filter ${filter.javaClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
        }
    }

    override fun hasFilters(): Boolean = filters.isNotEmpty()
}