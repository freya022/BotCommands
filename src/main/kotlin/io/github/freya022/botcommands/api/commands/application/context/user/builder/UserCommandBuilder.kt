package io.github.freya022.botcommands.api.commands.application.context.user.builder

import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionRegistry

interface UserCommandBuilder : TopLevelApplicationCommandBuilder<UserCommandOptionAggregateBuilder>,
                               UserCommandOptionRegistry
