package io.github.freya022.botcommands.api.commands.application.slash.options.builder

import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionAggregateBuilder

interface SlashCommandOptionAggregateBuilder : ApplicationCommandOptionAggregateBuilder<SlashCommandOptionAggregateBuilder>,
                                               SlashOptionRegistry
