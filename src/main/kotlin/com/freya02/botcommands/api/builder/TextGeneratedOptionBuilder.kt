package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.internal.GeneratedMethodParameter
import com.freya02.botcommands.internal.prefixed.TextGeneratedMethodParameter
import kotlin.reflect.KParameter

class TextGeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: TextGeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter =
        TextGeneratedMethodParameter(parameter, this)
}
