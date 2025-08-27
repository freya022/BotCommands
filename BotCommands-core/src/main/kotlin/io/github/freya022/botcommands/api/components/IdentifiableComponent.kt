package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.interactions.components.ActionComponent

interface IdentifiableComponent {
    val internalId: Int

    companion object {
        @JvmStatic
        fun isCompatible(id: String): Boolean = ComponentController.isCompatibleComponent(id)

        @JvmSynthetic
        fun ActionComponent.toIdentifiableComponent(): IdentifiableComponent = fromComponent(this)
        @JvmSynthetic
        fun ActionComponent.toIdentifiableComponentOrNull(): IdentifiableComponent? = fromComponentOrNull(this)

        @JvmStatic
        fun fromComponent(component: ActionComponent): IdentifiableComponent =
            fromId(component.id ?: throwArgument("This component has no ID"))

        @JvmStatic
        fun fromComponentOrNull(component: ActionComponent): IdentifiableComponent? =
            fromIdOrNull(component.id ?: throwArgument("This component has no ID"))

        @JvmStatic
        fun fromId(id: String): IdentifiableComponent {
            require(isCompatible(id)) {
                "Incompatible component id: '$id'"
            }
            return object : IdentifiableComponent {
                override val internalId = ComponentController.parseComponentId(id)
            }
        }

        @JvmStatic
        fun fromIdOrNull(id: String): IdentifiableComponent? {
            if (!isCompatible(id)) return null
            return object : IdentifiableComponent {
                override val internalId = ComponentController.parseComponentId(id)
            }
        }
    }
}
