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
    IConstrainableComponent by ConstrainableComponentImpl(),
    IUniqueComponent by UniqueComponentImpl(),
    BaseComponentBuilder,
    IPersistentActionableComponent by PersistentActionableComponentImpl(componentController.context),
    IPersistentTimeoutableComponent by PersistentTimeoutableComponentImpl() {
    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): JDAStringSelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot build on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun build(): Nothing {
        throwUser("Cannot build on components managed by the framework")
    }

    @JvmSynthetic
    internal fun doBuild(): StringSelectMenu {
        super.setId(componentController.createComponent(this))

        return StringSelectMenu(componentController, super.build())
    }
}