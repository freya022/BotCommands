package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.components.attribute.ICustomId

interface IdentifiableComponent {
    val internalId: Int

    companion object {
        @JvmStatic
        fun isCompatible(id: String): Boolean = ComponentController.isCompatibleComponent(id)

        @JvmSynthetic
        fun ICustomId.toIdentifiableComponent(): IdentifiableComponent = fromComponent(this)
        @JvmSynthetic
        fun ICustomId.toIdentifiableComponentOrNull(): IdentifiableComponent? = fromComponentOrNull(this)

        @JvmStatic
        fun fromComponent(component: ICustomId): IdentifiableComponent =
            fromId(component.customId ?: throwArgument("This component has no ID"))

        @JvmStatic
        fun fromComponentOrNull(component: ICustomId): IdentifiableComponent? =
            fromIdOrNull(component.customId ?: throwArgument("This component has no ID"))

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
