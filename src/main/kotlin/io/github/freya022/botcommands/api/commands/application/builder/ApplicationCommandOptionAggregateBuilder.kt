package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.CommandOptionAggregateBuilder

interface ApplicationCommandOptionAggregateBuilder<T> : CommandOptionAggregateBuilder<T>,
                                                        ApplicationOptionRegistry<T> where T : ApplicationCommandOptionAggregateBuilder<T>
