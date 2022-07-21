package com.freya02.botcommands.internal.events

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KParameter

class EventListenerParameter(parameter: KParameter, optionBuilder: OptionBuilder) : CommandParameter(parameter, optionBuilder)