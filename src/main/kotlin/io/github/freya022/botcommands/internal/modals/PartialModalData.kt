package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.ModalTimeoutInfo

internal open class PartialModalData(
    override val handlerData: IModalHandlerData?,
    override val inputDataMap: Map<String, InputData>,
    override val timeoutInfo: ModalTimeoutInfo?
) : IPartialModalData