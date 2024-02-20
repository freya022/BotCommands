package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TLongObjectMap

internal interface IPartialModalData {
    val handlerData: IModalHandlerData?
    val inputDataMap: TLongObjectMap<InputData>
    val timeoutInfo: ModalTimeoutInfo?
}