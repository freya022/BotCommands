package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.builder.ApplicationCommandOptionBuilder
import kotlin.reflect.KParameter

abstract class ApplicationCommandParameter(
    parameter: KParameter, optionBuilder: ApplicationCommandOptionBuilder
) : CommandParameter(parameter, optionBuilder) {

}