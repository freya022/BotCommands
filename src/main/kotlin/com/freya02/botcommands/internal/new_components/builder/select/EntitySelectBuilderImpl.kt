package com.freya02.botcommands.internal.new_components.builder.select

import com.freya02.botcommands.api.new_components.builder.select.EntitySelectBuilder

internal abstract class EntitySelectBuilderImpl<T : EntitySelectBuilder<T>> : SelectBuilderImpl<T>(), EntitySelectBuilder<T> {

}