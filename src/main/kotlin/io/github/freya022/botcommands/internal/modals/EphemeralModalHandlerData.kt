package io.github.freya022.botcommands.internal.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

internal class EphemeralModalHandlerData(val handler: suspend (ModalInteractionEvent) -> Unit) : IModalHandlerData