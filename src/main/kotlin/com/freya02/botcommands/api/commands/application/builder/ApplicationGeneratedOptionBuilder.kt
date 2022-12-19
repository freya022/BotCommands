package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import kotlin.reflect.KParameter

class ApplicationGeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: ApplicationGeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
), GeneratedOptionBuilder {
    override fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter =
        ApplicationGeneratedMethodParameter(parameter, this)
}
