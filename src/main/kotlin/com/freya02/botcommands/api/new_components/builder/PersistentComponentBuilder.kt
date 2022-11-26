package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.PersistentHandler
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout

//TODO use
interface PersistentComponentBuilder : ComponentBuilder {
    override val timeout: PersistentTimeout?
    override val handler: PersistentHandler?
}