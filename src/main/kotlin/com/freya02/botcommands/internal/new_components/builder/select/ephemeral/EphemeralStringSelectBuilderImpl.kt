package com.freya02.botcommands.internal.new_components.builder.select.ephemeral

import com.freya02.botcommands.api.new_components.builder.IConstrainableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.IUniqueComponent
import com.freya02.botcommands.api.new_components.builder.select.ephemeral.EphemeralStringSelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.builder.ConstrainableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.EphemeralActionableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.EphemeralTimeoutableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.UniqueComponentImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

internal class EphemeralStringSelectBuilderImpl(private val componentController: ComponentController) :
    StringSelectMenu.Builder(""),
    IConstrainableComponent<EphemeralStringSelectBuilder> by ConstrainableComponentImpl(),
    IUniqueComponent<EphemeralStringSelectBuilder> by UniqueComponentImpl(),
    EphemeralStringSelectBuilder,
    IEphemeralActionableComponent<EphemeralStringSelectBuilder> by EphemeralActionableComponentImpl(),
    IEphemeralTimeoutableComponent<EphemeralStringSelectBuilder> by EphemeralTimeoutableComponentImpl() {
    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    @Deprecated("Cannot get an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun getId(): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): Nothing {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    @Deprecated("Cannot build on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun build(): Nothing {
        throwUser("Cannot build on components managed by the framework")
    }

    internal fun doBuild(): StringSelectMenu {
        require(handler != null) {
            throwUser("A component handler needs to be set using #bindTo methods")
        }

        super.setId(componentController.createComponent(this))

        return super.build()
    }
}