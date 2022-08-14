package com.freya02.botcommands.api.builder

import com.freya02.botcommands.internal.GeneratedMethodParameter
import kotlin.reflect.KParameter

interface GeneratedOptionBuilder {
    fun toGeneratedMethodParameter(parameter: KParameter): GeneratedMethodParameter
}