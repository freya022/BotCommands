package io.github.freya022.botcommands.api.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.UniqueComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class PersistentStringSelectBuilder internal constructor(private val componentController: ComponentController) :
    JDAStringSelectMenu.Builder(""),
    IConstrainableComponent<PersistentStringSelectBuilder> by ConstrainableComponentImpl(),
    IUniqueComponent<PersistentStringSelectBuilder> by UniqueComponentImpl(),
    BaseComponentBuilder<PersistentStringSelectBuilder>,
    IPersistentActionableComponent<PersistentStringSelectBuilder> by PersistentActionableComponentImpl(componentController.context),
    IPersistentTimeoutableComponent<PersistentStringSelectBuilder> by PersistentTimeoutableComponentImpl() {

    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    override val instance: PersistentStringSelectBuilder = this

    private var built = false

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): JDAStringSelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwUser("Cannot set an ID on components managed by the framework")
    }

    override fun build(): StringSelectMenu {
        check(built) { "Cannot build components more than once" }
        built = true

        super.setId(componentController.createComponent(this))

        return StringSelectMenu(componentController, super.build())
    }
}