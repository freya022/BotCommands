package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.modals.ModalTimeoutInfo

internal open class PartialModalData(
    override val handlerData: IModalHandlerData?,
    override val inputDataMap: Map<String, InputData>,
    override val timeoutInfo: ModalTimeoutInfo?
) : IPartialModalData