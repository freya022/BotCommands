package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class StringSelectMenu internal constructor(
    private val componentController: ComponentController,
    private val selectMenu: JDAStringSelectMenu
) : JDAStringSelectMenu by selectMenu, IdentifiableComponent {
    override fun withDisabled(disabled: Boolean): StringSelectMenu {
        return StringSelectMenu(componentController, super.withDisabled(disabled))
    }

    override fun getId(): String = selectMenu.id ?: throwInternal("BC components cannot have null IDs")

    @JvmSynthetic
    override suspend fun await(): StringSelectEvent = componentController.awaitComponent(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringSelectMenu

        return selectMenu == other.selectMenu
    }

    override fun hashCode(): Int {
        return selectMenu.hashCode()
    }

    override fun toString(): String {
        return selectMenu.toString()
    }
}