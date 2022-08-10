package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.modals.ModalTimeoutInfo

open class PartialModalData(
    override val handlerName: String,
    override val userData: Array<Any>,
    override val inputDataMap: Map<String, InputData>,
    override val timeoutInfo: ModalTimeoutInfo?
) : IPartialModalData