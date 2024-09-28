package io.github.freya022.botcommands.internal.components.builder.mixin

import io.github.freya022.botcommands.api.components.builder.IConstrainableComponent
import io.github.freya022.botcommands.internal.components.builder.BuilderInstanceHolder

internal interface IConstrainableComponentMixin<T : IConstrainableComponent<T>> : IConstrainableComponent<T>,
                                                                                  BuilderInstanceHolder<T>