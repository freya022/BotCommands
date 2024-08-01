package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder

interface CommandOptionAggregateBuilder<T : CommandOptionAggregateBuilder<T>> : OptionAggregateBuilder<T>
