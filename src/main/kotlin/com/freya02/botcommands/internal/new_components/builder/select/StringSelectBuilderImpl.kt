package com.freya02.botcommands.internal.new_components.builder.select

import com.freya02.botcommands.api.new_components.builder.select.StringSelectBuilder

internal abstract class StringSelectBuilderImpl<T : StringSelectBuilder<T>> :
    SelectBuilderImpl<T>(),
    StringSelectBuilder<T> {

}