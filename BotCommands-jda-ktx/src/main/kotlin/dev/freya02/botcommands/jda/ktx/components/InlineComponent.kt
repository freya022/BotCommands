package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.Component

@DslMarker
internal annotation class InlineComponentDSL

@InlineComponentDSL
internal interface InlineComponent {
    /** Unique identifier of this component, see [Component.withUniqueId] */
    var uniqueId: Int
}
