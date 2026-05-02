package io.github.freya022.botcommands.api.commands.application.context.message.builder

import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionRegistry

interface MessageCommandBuilder : TopLevelApplicationCommandBuilder<MessageCommandOptionAggregateBuilder>,
                                  MessageCommandOptionRegistry
