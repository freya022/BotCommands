package io.github.freya022.botcommands.internal.components.builder.mixin

import io.github.freya022.botcommands.api.components.builder.IUniqueComponent
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolder

internal interface IUniqueComponentMixin<T : IUniqueComponent<T>> : IUniqueComponent<T>,
                                                                    BuilderInstanceHolder<T>