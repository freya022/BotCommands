package io.github.freya022.botcommands.internal.components.builder.select.persistent

import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.StringSelectMenuImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.*
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.UniqueComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

@PublishedApi
internal class PersistentStringSelectBuilderImpl internal constructor(
    private val componentController: ComponentController,
    instanceRetriever: InstanceRetriever<PersistentStringSelectBuilder>
) : PersistentStringSelectBuilder(),
    BaseComponentBuilderMixin<PersistentStringSelectBuilder>,
    IConstrainableComponentMixin<PersistentStringSelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponentMixin<PersistentStringSelectBuilder> by UniqueComponentImpl(instanceRetriever),
    IPersistentActionableComponentMixin<PersistentStringSelectBuilder> by PersistentActionableComponentImpl(
        componentController.context,
        instanceRetriever
    ),
    IPersistentTimeoutableComponentMixin<PersistentStringSelectBuilder> by PersistentTimeoutableComponentImpl(
        instanceRetriever
    ) {

    override val componentType: ComponentType get() = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentStringSelectBuilderImpl get() = this

    private var built = false

    init {
        instanceRetriever.instance = this
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun setId(customId: String): StringSelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    override fun build(): io.github.freya022.botcommands.api.components.StringSelectMenu =
        runBlocking { buildSuspend() }

    @JvmSynthetic
    @PublishedApi
    internal suspend fun buildSuspend(): io.github.freya022.botcommands.api.components.StringSelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            setId(componentId)
            return StringSelectMenuImpl(componentController, internalId, jdaBuild())
        }
    }
}