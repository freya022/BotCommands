package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.commands.prefixed.TextGeneratedMethodParameter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class TextGeneratedOptionBuilder(
    owner: KFunction<*>,
    declaredName: String,
    val generatedValueSupplier: TextGeneratedValueSupplier
) : OptionBuilder(owner, declaredName), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter =
        TextGeneratedMethodParameter(parameter, this)
}
