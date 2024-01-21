package io.github.freya022.botcommands.internal.modals

internal open class PartialModalData(
    override val handlerData: IModalHandlerData?,
    override val inputDataMap: Map<String, InputData>,
    override val timeoutInfo: ModalTimeoutInfo?
) : IPartialModalData