package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

interface StringSelectMenu : JDAStringSelectMenu,
                             AwaitableComponent<StringSelectEvent>,
                             IGroupHolder {

    override fun asEnabled(): StringSelectMenu = withDisabled(false)

    override fun asDisabled(): StringSelectMenu = withDisabled(true)

    override fun withDisabled(disabled: Boolean): StringSelectMenu

    override fun getId(): String
}