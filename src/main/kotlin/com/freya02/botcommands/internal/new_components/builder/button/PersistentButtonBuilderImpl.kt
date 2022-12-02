package com.freya02.botcommands.internal.new_components.builder.button

import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.button.PersistentButtonBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.PersistentActionableComponentImpl
import com.freya02.botcommands.internal.new_components.builder.PersistentTimeoutableComponentImpl
import com.freya02.botcommands.internal.new_components.new.ComponentController
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

internal class PersistentButtonBuilderImpl internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilderImpl<PersistentButtonBuilder>(componentController, style),
    PersistentButtonBuilder,
    IPersistentActionableComponent<PersistentButtonBuilder> by PersistentActionableComponentImpl(),
    IPersistentTimeoutableComponent<PersistentButtonBuilder> by PersistentTimeoutableComponentImpl() {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}