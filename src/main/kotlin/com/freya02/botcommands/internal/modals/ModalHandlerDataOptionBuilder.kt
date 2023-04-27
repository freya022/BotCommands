package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import kotlin.reflect.KFunction

internal class ModalHandlerDataOptionBuilder(owner: KFunction<*>, declaredName: String) : OptionBuilder(owner, declaredName), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter() = ModalHandlerDataOption(this)
}
