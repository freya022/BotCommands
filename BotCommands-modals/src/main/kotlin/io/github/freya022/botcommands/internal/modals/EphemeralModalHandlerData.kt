package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.ModalEvent

internal class EphemeralModalHandlerData(val handler: suspend (ModalEvent) -> Unit) : IModalHandlerData