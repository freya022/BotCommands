package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter

abstract class CommandOptionBuilder(multiParameter: MultiParameter, val optionName: String) : OptionBuilder(multiParameter)
