package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.prefixed.autobuilder.metadata.TextFunctionMetadata
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import mu.KotlinLogging
import kotlin.math.min
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal class TextCommandComparator(private val context: BContextImpl) : Comparator<TextFunctionMetadata> {
    private val logger = KotlinLogging.logger {  }

    private val TextFunctionMetadata.optionParameters
        get() = func.nonInstanceParameters
            .drop(1)
            .filter { context.getService<ResolverContainer>().getResolverOrNull(it) is RegexParameterResolver<*, *> }

    //TODO is this correct ? the same old implementation did not function with the new objects.
    override fun compare(o1: TextFunctionMetadata, o2: TextFunctionMetadata): Int {
        if (o1.func == o2.func) return 0

        //Put command with options first
        if (o1.optionParameters.any() && !o2.optionParameters.any()) {
            return -1
        } else if (!o1.optionParameters.any() && o2.optionParameters.any()) {
            return 1
        }

        val order1 = o1.annotation.order
        val order2 = o2.annotation.order
        if (order1 != 0 && order2 != 0) {
            if (order1 == order2) {
                logger.warn(
                    "Method {} and {} have the same order ({})",
                    o1.func.shortSignatureNoSrc,
                    o2.func.shortSignatureNoSrc,
                    order1
                )
            }

            return order1.compareTo(order2)
        }

        val o1Parameters: List<KParameter> = o1.optionParameters
        val o2Parameters: List<KParameter> = o2.optionParameters
        for (i in 0 until min(o1Parameters.size, o2Parameters.size)) {
            if (o1Parameters[i].type == o2Parameters[i].type) {
                continue
            }

            return when (o1Parameters[i].type.jvmErasure) {
                String::class -> 1
                else -> -1
            }
        }

        return 1
    }
}