package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder

interface SlashCommandOptionAggregateBuilder : ApplicationCommandOptionAggregateBuilder<SlashCommandOptionAggregateBuilder>,
                                               SlashOptionRegistry
