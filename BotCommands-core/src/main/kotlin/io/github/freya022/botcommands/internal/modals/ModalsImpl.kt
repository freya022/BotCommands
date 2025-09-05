package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals

@BService
@RequiresModals
internal class ModalsImpl(private val modalMaps: ModalMaps) : Modals {
    override fun create(title: String): ModalBuilderImpl {
        return ModalBuilderImpl(this, modalMaps, title)
    }
}
