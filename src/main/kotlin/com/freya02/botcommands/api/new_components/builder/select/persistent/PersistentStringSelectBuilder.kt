package com.freya02.botcommands.api.new_components.builder.select.persistent

import com.freya02.botcommands.api.new_components.builder.*
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.builder.ConstrainableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.PersistentActionableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.PersistentTimeoutableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.UniqueComponentImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class PersistentStringSelectBuilder internal constructor(private val componentController: ComponentController) :
    StringSelectMenu.Builder(""),
    IConstrainableComponent<PersistentStringSelectBuilder> by ConstrainableComponentImpl(),
    IUniqueComponent<PersistentStringSelectBuilder> by UniqueComponentImpl(),
    ComponentBuilder<PersistentStringSelectBuilder>,
    IPersistentActionableComponent<PersistentStringSelectBuilder> by PersistentActionableComponentImpl(),
    IPersistentTimeoutableComponent<PersistentStringSelectBuilder> by PersistentTimeoutableComponentImpl() {
    override val componentType: ComponentType = ComponentType.SELECT_MENU
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

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

    @JvmSynthetic
    internal fun doBuild(): StringSelectMenu {
        require(handler != null) {
            throwUser("A component handler needs to be set using #bindTo methods")
        }

        super.setId(componentController.createComponent(this))

        return super.build()
    }
}