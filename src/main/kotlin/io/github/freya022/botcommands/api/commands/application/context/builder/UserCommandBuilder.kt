package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder

interface UserCommandBuilder : ApplicationCommandBuilder<UserCommandOptionAggregateBuilder>,
                               UserCommandOptionRegistry,
                               ITopLevelApplicationCommandBuilder
