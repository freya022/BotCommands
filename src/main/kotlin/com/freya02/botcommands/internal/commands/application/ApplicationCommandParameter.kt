package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.internal.commands.CommandParameter
import kotlin.reflect.KParameter

abstract class ApplicationCommandParameter(
    parameter: KParameter, optionBuilder: ApplicationCommandOptionBuilder
) : CommandParameter(parameter, optionBuilder) {

}