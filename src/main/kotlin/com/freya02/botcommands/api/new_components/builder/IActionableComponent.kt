package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.ComponentHandler

interface IActionableComponent {
    val handler: ComponentHandler?
}