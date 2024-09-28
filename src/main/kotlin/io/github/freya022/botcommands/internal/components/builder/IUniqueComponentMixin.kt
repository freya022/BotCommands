package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IUniqueComponent

internal interface IUniqueComponentMixin<T : IUniqueComponent<T>> : IUniqueComponent<T>,
                                                                    BuilderInstanceHolder<T>