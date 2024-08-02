package io.github.freya022.botcommands.api.commands.text.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder

interface TextCommandOptionAggregateBuilder : OptionAggregateBuilder<TextCommandOptionAggregateBuilder>,
                                              TextOptionRegistry
