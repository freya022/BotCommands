package io.github.freya022.botcommands.api.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

abstract class PersistentEntitySelectBuilder :
        JDAEntitySelectMenu.Builder(""),
        BaseComponentBuilder<PersistentEntitySelectBuilder>,
        IConstrainableComponent<PersistentEntitySelectBuilder>,
        IUniqueComponent<PersistentEntitySelectBuilder>,
        IPersistentActionableComponent<PersistentEntitySelectBuilder>,
        IPersistentTimeoutableComponent<PersistentEntitySelectBuilder> {

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun getId(): Nothing

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun setId(customId: String): JDAEntitySelectMenu.Builder

    abstract override fun build(): EntitySelectMenu

    protected fun jdaBuild(id: String): JDAEntitySelectMenu {
        super.setId(id)
        return super.build()
    }
}