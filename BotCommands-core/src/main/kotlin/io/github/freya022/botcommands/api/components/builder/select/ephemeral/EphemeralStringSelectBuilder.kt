package io.github.freya022.botcommands.api.components.builder.select.ephemeral

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import net.dv8tion.jda.api.components.selections.StringSelectMenu as JDAStringSelectMenu

abstract class EphemeralStringSelectBuilder :
        JDAStringSelectMenu.Builder(""),
        BaseComponentBuilder<EphemeralStringSelectBuilder>,
        IConstrainableComponent<EphemeralStringSelectBuilder>,
        IUniqueComponent<EphemeralStringSelectBuilder>,
        IEphemeralActionableComponent<EphemeralStringSelectBuilder, StringSelectEvent>,
        IEphemeralTimeoutableComponent<EphemeralStringSelectBuilder> {

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun setCustomId(customId: String): JDAStringSelectMenu.Builder

    abstract override fun build(): StringSelectMenu

    protected fun jdaBuild(id: String): JDAStringSelectMenu {
        super.setCustomId(id)
        return super.build()
    }
}
