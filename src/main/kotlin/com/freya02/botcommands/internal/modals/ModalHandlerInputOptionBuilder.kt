package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import kotlin.reflect.KFunction

internal class ModalHandlerInputOptionBuilder(owner: KFunction<*>, declaredName: String) : OptionBuilder(owner, declaredName)
