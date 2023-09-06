package com.freya02.botcommands.internal.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

internal class EphemeralModalHandlerData(val handler: suspend (ModalInteractionEvent) -> Unit) : IModalHandlerData