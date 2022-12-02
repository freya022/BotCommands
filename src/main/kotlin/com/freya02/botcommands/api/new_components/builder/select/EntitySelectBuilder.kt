package com.freya02.botcommands.api.new_components.builder.select

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu

interface EntitySelectBuilder<T : EntitySelectBuilder<T>> : SelectBuilder<T> {
    fun build(): EntitySelectMenu
}