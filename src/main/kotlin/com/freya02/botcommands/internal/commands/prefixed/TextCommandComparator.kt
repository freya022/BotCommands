package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignatureNoSrc
import kotlin.math.min
import kotlin.reflect.jvm.jvmErasure

object TextCommandComparator : Comparator<TextCommandInfo> {
    private val LOGGER = Logging.getLogger()

    //TODO is this correct ? the same old implementation did not function with the new objects.
    override fun compare(o1: TextCommandInfo, o2: TextCommandInfo): Int {
        if (o1.method == o2.method) return 0

        //Put command with options first
        if (o1.optionParameters.any() && !o2.optionParameters.any()) {
            return -1
        } else if (!o1.optionParameters.any() && o2.optionParameters.any()) {
            return 1
        }

        val order1 = o1.order
        val order2 = o2.order
        if (order1 != 0 && order2 != 0) {
            if (order1 == order2) {
                LOGGER.warn(
                    "Method {} and {} have the same order ({})",
                    o1.method.shortSignatureNoSrc,
                    o2.method.shortSignatureNoSrc,
                    order1
                )
            }

            return order1.compareTo(order2)
        }

        val o1Parameters: List<TextCommandParameter> = o1.optionParameters
        val o2Parameters: List<TextCommandParameter> = o2.optionParameters
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