package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IConstrainableComponent

internal interface IConstrainableComponentMixin<T : IConstrainableComponent<T>> : IConstrainableComponent<T>,
                                                                                  BuilderInstanceHolder<T>