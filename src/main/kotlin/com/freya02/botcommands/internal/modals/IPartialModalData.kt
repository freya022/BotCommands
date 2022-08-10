package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.modals.ModalTimeoutInfo

interface IPartialModalData {
    val handlerName: String
    val userData: Array<Any>
    val inputDataMap: Map<String, InputData>
    val timeoutInfo: ModalTimeoutInfo?
}