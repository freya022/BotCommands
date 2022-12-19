package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import kotlin.reflect.KParameter

interface GeneratedOptionBuilder {
    fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter
}