package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.internal.components.controller.ComponentController

interface IdentifiableComponent {
    val internalId: Int

    companion object {
        @JvmStatic
        fun isCompatible(id: String): Boolean = ComponentController.isCompatibleComponent(id)

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