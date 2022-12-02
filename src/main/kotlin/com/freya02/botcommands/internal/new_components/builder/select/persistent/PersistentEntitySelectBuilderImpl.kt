package com.freya02.botcommands.internal.new_components.builder.select.persistent

import com.freya02.botcommands.api.new_components.builder.IConstrainableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.IUniqueComponent
import com.freya02.botcommands.api.new_components.builder.select.persistent.PersistentEntitySelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.builder.ConstrainableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.PersistentActionableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.PersistentTimeoutableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.UniqueComponentImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu

internal class PersistentEntitySelectBuilderImpl(private val componentController: ComponentController) :
    EntitySelectMenu.Builder(""),
    IConstrainableComponent<PersistentEntitySelectBuilder> by ConstrainableComponentImpl(),
    IUniqueComponent<PersistentEntitySelectBuilder> by UniqueComponentImpl(),
    PersistentEntitySelectBuilder,
    IPersistentActionableComponent<PersistentEntitySelectBuilder> by PersistentActionableComponentImpl(),
    IPersistentTimeoutableComponent<PersistentEntitySelectBuilder> by PersistentTimeoutableComponentImpl() {
    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    @Deprecated("Cannot set an ID on components managed by the framework", level = DeprecationLevel.ERROR)
    override fun setId(customId: String): EntitySelectMenu.Builder {
        throwUser("Cannot set an ID on components managed by the framework")
    }

    override fun build(): EntitySelectMenu {
        require(handler != null) {
            throwUser("A component handler needs to be set using #bindTo methods")
        }

        super.setId(componentController.createComponent(this))

        return super.build()
    }
}