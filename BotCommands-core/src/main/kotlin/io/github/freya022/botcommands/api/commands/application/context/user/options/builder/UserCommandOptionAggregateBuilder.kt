package io.github.freya022.botcommands.api.commands.application.context.user.options.builder

import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionAggregateBuilder

interface UserCommandOptionAggregateBuilder :
        ApplicationCommandOptionAggregateBuilder<UserCommandOptionAggregateBuilder>,
        UserCommandOptionRegistry
