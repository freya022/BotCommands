package io.github.freya022.botcommands.api.commands.application.context.message.options.builder

import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionAggregateBuilder

interface MessageCommandOptionAggregateBuilder :
        ApplicationCommandOptionAggregateBuilder<MessageCommandOptionAggregateBuilder>,
        MessageCommandOptionRegistry
