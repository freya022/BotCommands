package io.github.freya022.botcommands.api.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.*
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.*
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

class PersistentStringSelectBuilder internal constructor(
    private val componentController: ComponentController,
    instanceRetriever: InstanceRetriever<PersistentStringSelectBuilder>
) : JDAStringSelectMenu.Builder(""),
    IConstrainableComponent<PersistentStringSelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponent<PersistentStringSelectBuilder> by UniqueComponentImpl(instanceRetriever),
    BaseComponentBuilder<PersistentStringSelectBuilder>,
    IPersistentActionableComponent<PersistentStringSelectBuilder> by PersistentActionableComponentImpl(componentController.context, instanceRetriever),
    IPersistentTimeoutableComponent<PersistentStringSelectBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    override val instance: PersistentStringSelectBuilder = this

    private var built = false

    init {
        instanceRetriever.instance = this
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): JDAStringSelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwUser("Cannot set an ID on components managed by the framework")
    }

    override fun build(): StringSelectMenu = runBlocking { buildSuspend() }

    @JvmSynthetic
    @PublishedApi
    internal suspend fun buildSuspend(): StringSelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            super.setId(componentId)
            return StringSelectMenu(componentController, internalId, super.build())
        }
    }
}