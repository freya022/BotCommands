package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder

interface MessageCommandOptionAggregateBuilder :
        ApplicationCommandOptionAggregateBuilder<MessageCommandOptionAggregateBuilder>,
        MessageCommandOptionRegistry
