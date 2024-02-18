package io.github.freya022.botcommands.api.components.builder.select.ephemeral

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.*
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class EphemeralEntitySelectBuilder internal constructor(
    private val componentController: ComponentController,
    targets: Collection<SelectTarget>,
    instanceRetriever: InstanceRetriever<EphemeralEntitySelectBuilder>
) : JDAEntitySelectMenu.Builder(""),
    IConstrainableComponent<EphemeralEntitySelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponent<EphemeralEntitySelectBuilder> by UniqueComponentImpl(instanceRetriever),
    BaseComponentBuilder<EphemeralEntitySelectBuilder>,
    IEphemeralActionableComponent<EphemeralEntitySelectBuilder, EntitySelectEvent> by EphemeralActionableComponentImpl(componentController.context, instanceRetriever),
    IEphemeralTimeoutableComponent<EphemeralEntitySelectBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
    override val instance: EphemeralEntitySelectBuilder = this

    private var built = false

    init {
        instanceRetriever.instance = this
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

    override fun build(): EntitySelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            super.setId(componentId)
            return EntitySelectMenu(componentController, internalId, super.build())
        }
    }
}