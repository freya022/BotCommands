package io.github.freya022.botcommands.api.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

abstract class PersistentStringSelectBuilder :
        JDAStringSelectMenu.Builder(""),
        BaseComponentBuilder<PersistentStringSelectBuilder>,
        IConstrainableComponent<PersistentStringSelectBuilder>,
        IUniqueComponent<PersistentStringSelectBuilder>,
        IPersistentActionableComponent<PersistentStringSelectBuilder>,
        IPersistentTimeoutableComponent<PersistentStringSelectBuilder> {

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun getId(): Nothing

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun setId(customId: String): JDAStringSelectMenu.Builder

    abstract override fun build(): StringSelectMenu

    protected fun jdaBuild(id: String): JDAStringSelectMenu {
        super.setId(id)
        return super.build()
    }
}