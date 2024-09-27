package io.github.freya022.botcommands.api.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.EntitySelectMenuImpl
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.*
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

class PersistentEntitySelectBuilder internal constructor(
    private val componentController: ComponentController,
    targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    instanceRetriever: InstanceRetriever<PersistentEntitySelectBuilder>
) : JDAEntitySelectMenu.Builder(""),
    IConstrainableComponent<PersistentEntitySelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponent<PersistentEntitySelectBuilder> by UniqueComponentImpl(instanceRetriever),
    BaseComponentBuilder<PersistentEntitySelectBuilder>,
    IPersistentActionableComponent<PersistentEntitySelectBuilder> by PersistentActionableComponentImpl(componentController.context, instanceRetriever),
    IPersistentTimeoutableComponent<PersistentEntitySelectBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val componentType: ComponentType get() = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentEntitySelectBuilder get() = this

    private var built = false

    init {
        instanceRetriever.instance = this
        setEntityTypes(targets)
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): JDAEntitySelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    override fun build(): EntitySelectMenu = runBlocking { buildSuspend() }

    @JvmSynthetic
    @PublishedApi
    internal suspend fun buildSuspend(): EntitySelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            super.setId(componentId)
            return EntitySelectMenuImpl(componentController, internalId, super.build())
        }
    }
}