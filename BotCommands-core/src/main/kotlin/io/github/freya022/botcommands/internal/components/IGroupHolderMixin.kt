package io.github.freya022.botcommands.internal.components

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.IGroupHolder

internal interface IGroupHolderMixin : IGroupHolder {
    override var group: ComponentGroup?
}