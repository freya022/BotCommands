package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionRegistry

interface ExecutableCommandBuilder<T : OptionAggregateBuilder<T>> : CommandBuilder,
                                                                    OptionRegistry<T>
