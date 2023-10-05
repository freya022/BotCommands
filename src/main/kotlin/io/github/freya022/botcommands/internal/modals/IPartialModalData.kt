package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.ModalTimeoutInfo

internal interface IPartialModalData {
    val handlerData: IModalHandlerData?
    val inputDataMap: Map<String, InputData>
    val timeoutInfo: ModalTimeoutInfo?
}