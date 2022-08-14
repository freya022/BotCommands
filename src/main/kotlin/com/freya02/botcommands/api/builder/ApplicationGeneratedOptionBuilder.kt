package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.internal.GeneratedMethodParameter
import com.freya02.botcommands.internal.application.slash.ApplicationGeneratedMethodParameter
import kotlin.reflect.KParameter

class ApplicationGeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: ApplicationGeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter =
        ApplicationGeneratedMethodParameter(parameter, this)
}
