package io.github.freya022.botcommands.api.commands.application.options.builder

import io.github.freya022.botcommands.api.commands.options.builder.CommandOptionAggregateBuilder

interface ApplicationCommandOptionAggregateBuilder<T> : CommandOptionAggregateBuilder<T>,
                                                        ApplicationOptionRegistry<T> where T : ApplicationCommandOptionAggregateBuilder<T>
