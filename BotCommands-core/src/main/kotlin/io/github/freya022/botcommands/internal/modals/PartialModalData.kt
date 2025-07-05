package io.github.freya022.botcommands.internal.modals

import gnu.trove.map.TLongObjectMap

internal open class PartialModalData(
    override val handlerData: IModalHandlerData?,
    override val inputDataMap: TLongObjectMap<InputData>,
    override val timeoutInfo: ModalTimeoutInfo?
) : IPartialModalData