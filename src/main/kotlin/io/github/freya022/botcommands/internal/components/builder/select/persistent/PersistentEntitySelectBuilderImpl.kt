package io.github.freya022.botcommands.internal.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.EntitySelectMenuImpl
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.*
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.UniqueComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu as JDAEntitySelectMenu

@PublishedApi
internal class PersistentEntitySelectBuilderImpl internal constructor(
    private val componentController: ComponentController,
    targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    instanceRetriever: InstanceRetriever<PersistentEntitySelectBuilder>
) : PersistentEntitySelectBuilder(),
    BaseComponentBuilderMixin<PersistentEntitySelectBuilder>,
    IConstrainableComponentMixin<PersistentEntitySelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponentMixin<PersistentEntitySelectBuilder> by UniqueComponentImpl(instanceRetriever),
    IPersistentActionableComponentMixin<PersistentEntitySelectBuilder> by PersistentActionableComponentImpl(componentController.context, instanceRetriever),
    IPersistentTimeoutableComponentMixin<PersistentEntitySelectBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val componentType: ComponentType get() = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentEntitySelectBuilderImpl get() = this

    private var built = false

    init {
        instanceRetriever.instance = this
        setEntityTypes(targets)
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun setId(customId: String): JDAEntitySelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    override fun build(): EntitySelectMenu = runBlocking { buildSuspend() }

    @PublishedApi
    internal suspend fun buildSuspend(): EntitySelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            return EntitySelectMenuImpl(componentController, internalId, jdaBuild(componentId))
        }
    }
}