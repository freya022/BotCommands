package com.freya02.botcommands.api.components.builder.select.ephemeral

import com.freya02.botcommands.api.components.EntitySelectMenu
import com.freya02.botcommands.api.components.builder.*
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.builder.ConstrainableComponentImpl
import com.freya02.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import com.freya02.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import com.freya02.botcommands.internal.components.builder.UniqueComponentImpl
import com.freya02.botcommands.internal.components.controller.ComponentController
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EphemeralEntitySelectBuilder internal constructor(private val componentController: ComponentController, targets: List<SelectTarget>) :
    JDAEntitySelectMenu.Builder(""),
    IConstrainableComponent by ConstrainableComponentImpl(),
    IUniqueComponent by UniqueComponentImpl(),
    BaseComponentBuilder,
    IEphemeralActionableComponent<EntitySelectEvent> by EphemeralActionableComponentImpl(),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl() {
    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    init {
        setEntityTypes(targets)
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): JDAEntitySelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot build on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun build(): Nothing {
        throwUser("Cannot build on components managed by the framework")
    }

    internal fun doBuild(): EntitySelectMenu {
        super.setId(componentController.createComponent(this))

        return EntitySelectMenu(componentController, super.build())
    }
}