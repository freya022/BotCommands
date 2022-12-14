package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.new_components.builder.ConstrainableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.UniqueComponentImpl

abstract class AbstractComponentBuilder internal constructor() :
    BaseComponentBuilder,
    IUniqueComponent by UniqueComponentImpl(),
    IConstrainableComponent by ConstrainableComponentImpl()