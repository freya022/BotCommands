package com.freya02.botcommands.internal.new_components

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

interface ComponentHandler

class PersistentHandler(handlerName: String, args: Array<out Any?>) : ComponentHandler

class EphemeralHandler<T : GenericComponentInteractionCreateEvent>(handler: (T) -> Unit) : ComponentHandler