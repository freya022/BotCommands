package io.github.freya022.botcommands.internal.components.builder.select.ephemeral

import io.github.freya022.botcommands.api.components.StringSelectMenu
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.StringSelectMenuImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.*
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.EphemeralActionableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.UniqueComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu as JDAStringSelectMenu

@PublishedApi
internal class EphemeralStringSelectBuilderImpl internal constructor(
    private val componentController: ComponentController,
    instanceRetriever: InstanceRetriever<EphemeralStringSelectBuilder>
) : EphemeralStringSelectBuilder(),
    BaseComponentBuilderMixin<EphemeralStringSelectBuilder>,
    IConstrainableComponentMixin<EphemeralStringSelectBuilder> by ConstrainableComponentImpl(instanceRetriever),
    IUniqueComponentMixin<EphemeralStringSelectBuilder> by UniqueComponentImpl(instanceRetriever),
    IEphemeralActionableComponentMixin<EphemeralStringSelectBuilder, StringSelectEvent> by EphemeralActionableComponentImpl(componentController.context, instanceRetriever),
    IEphemeralTimeoutableComponentMixin<EphemeralStringSelectBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val componentType: ComponentType get() = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType get() = LifetimeType.EPHEMERAL
    override val instance: EphemeralStringSelectBuilderImpl get() = this

    private var built = false

    init {
        instanceRetriever.instance = this
    }

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    @Suppress("OVERRIDE_DEPRECATION") // yup
    override fun setId(customId: String): JDAStringSelectMenu.Builder {
        if (customId.isEmpty()) return this //Empty ID is set by super constructor
        throwArgument("Cannot set an ID on components managed by the framework")
    }

    override fun build(): StringSelectMenu = runBlocking { buildSuspend() }

    @PublishedApi
    internal suspend fun buildSuspend(): StringSelectMenu {
        check(!built) { "Cannot build components more than once" }
        built = true

        componentController.withNewComponent(this) { internalId, componentId ->
            return StringSelectMenuImpl(componentController, internalId, jdaBuild(componentId))
        }
    }
}