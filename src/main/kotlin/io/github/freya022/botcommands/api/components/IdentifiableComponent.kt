package io.github.freya022.botcommands.api.components

interface IdentifiableComponent {
    val internalId: Int

    val group: ComponentGroup?
}