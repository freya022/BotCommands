package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

internal class StringSelectMenuImpl internal constructor(
    componentController: ComponentController,
    override val internalId: Int,
    private val selectMenu: JDAStringSelectMenu
) : AbstractAwaitableComponentImpl<StringSelectEvent>(componentController),
    StringSelectMenu,
    JDAStringSelectMenu by selectMenu {

    override fun withDisabled(disabled: Boolean): StringSelectMenuImpl {
        return StringSelectMenuImpl(componentController, internalId, super<JDAStringSelectMenu>.withDisabled(disabled))
    }

    override fun getId(): String = selectMenu.id ?: throwInternal("BC components cannot have null IDs")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringSelectMenuImpl

        return selectMenu == other.selectMenu
    }

    override fun hashCode(): Int {
        return selectMenu.hashCode()
    }

    override fun toString(): String {
        return selectMenu.toString()
    }
}