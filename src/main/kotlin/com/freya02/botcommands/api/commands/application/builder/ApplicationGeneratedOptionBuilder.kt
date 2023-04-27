package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import kotlin.reflect.KFunction

class ApplicationGeneratedOptionBuilder(
    owner: KFunction<*>,
    declaredName: String,
    val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : OptionBuilder(owner, declaredName), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter(): GeneratedMethodParameter =
        ApplicationGeneratedMethodParameter(this)
}
