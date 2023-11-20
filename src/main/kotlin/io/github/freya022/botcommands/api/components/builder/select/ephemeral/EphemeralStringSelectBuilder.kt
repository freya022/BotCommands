package io.github.freya022.botcommands.api.components.builder.select.ephemeral

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.EphemeralActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.UniqueComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class EphemeralStringSelectBuilder internal constructor(private val componentController: ComponentController) :
    JDAStringSelectMenu.Builder(""),
    IConstrainableComponent<EphemeralStringSelectBuilder> by ConstrainableComponentImpl(),
    IUniqueComponent<EphemeralStringSelectBuilder> by UniqueComponentImpl(),
    BaseComponentBuilder<EphemeralStringSelectBuilder>,
    IEphemeralActionableComponent<EphemeralStringSelectBuilder, StringSelectEvent> by EphemeralActionableComponentImpl(componentController.context),
    IEphemeralTimeoutableComponent<EphemeralStringSelectBuilder> by EphemeralTimeoutableComponentImpl() {

    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
    override val instance: EphemeralStringSelectBuilder = this

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