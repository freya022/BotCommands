package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.builder.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.UniqueComponentImpl

abstract class AbstractComponentBuilder internal constructor() :
    BaseComponentBuilder,
    IUniqueComponent by UniqueComponentImpl(),
    IConstrainableComponent by ConstrainableComponentImpl()