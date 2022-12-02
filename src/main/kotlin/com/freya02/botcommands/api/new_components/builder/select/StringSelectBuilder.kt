package com.freya02.botcommands.api.new_components.builder.select

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

interface StringSelectBuilder<T : StringSelectBuilder<T>> : SelectBuilder<T> {
    fun build(): StringSelectMenu
}