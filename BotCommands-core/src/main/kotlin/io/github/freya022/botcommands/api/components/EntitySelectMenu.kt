package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

interface EntitySelectMenu : JDAEntitySelectMenu,
                             AwaitableComponent<EntitySelectEvent>,
                             IGroupHolder {

    override fun asEnabled(): EntitySelectMenu = withDisabled(false)

    override fun asDisabled(): EntitySelectMenu = withDisabled(true)

    override fun withDisabled(disabled: Boolean): EntitySelectMenu

    override fun getId(): String
}