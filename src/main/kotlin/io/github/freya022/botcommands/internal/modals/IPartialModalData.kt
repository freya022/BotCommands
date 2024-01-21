package io.github.freya022.botcommands.internal.modals

internal interface IPartialModalData {
    val handlerData: IModalHandlerData?
    val inputDataMap: Map<String, InputData>
    val timeoutInfo: ModalTimeoutInfo?
}