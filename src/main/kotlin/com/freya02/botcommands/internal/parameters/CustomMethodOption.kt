package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

class CustomMethodOption(override val kParameter: KParameter, val resolver: ICustomResolver<*, *>) : AbstractOption {
    override val methodParameterType = MethodParameterType.CUSTOM

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}