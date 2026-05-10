package io.github.freya022.botcommands.api.commands.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder

interface CommandOptionAggregateBuilder<T : CommandOptionAggregateBuilder<T>> : OptionAggregateBuilder<T>
