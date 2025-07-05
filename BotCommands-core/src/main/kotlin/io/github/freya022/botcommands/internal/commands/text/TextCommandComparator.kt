package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.text.autobuilder.metadata.TextFunctionMetadata
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.min
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal class TextCommandComparator(private val context: BContext) : Comparator<TextFunctionMetadata> {
    private val logger = KotlinLogging.logger {  }

    private val TextFunctionMetadata.optionParameters
        get() = func.nonInstanceParameters
            .drop(1)
            .filter { context.getService<ResolverContainer>().hasResolverOfType<TextParameterResolver<*, *>>(it.wrap()) }

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
                logger.warn {
                    "Method ${o1.func.shortSignatureNoSrc} and ${o2.func.shortSignatureNoSrc} have the same order (${order1})"
                }
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