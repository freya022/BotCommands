package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.requireUser
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

object SlashUtils2 {
    @JvmStatic
    fun ExecutableInteractionInfo.checkDefaultValue(
        parameter: ApplicationCommandVarArgParameter<*>,
        defaultValue: Any?
    ) {
        requireUser(defaultValue != null || parameter.isOptional) {
            "Default value supplier for parameter #${parameter.index} has returned a null value but parameter is not optional"
        }

        if (defaultValue == null) return

        val expectedType: KClass<*> = if (parameter.isVarArg) List::class else parameter.boxedType.jvmErasure

        requireUser(expectedType.isSuperclassOf(defaultValue::class)) {
            "Default value supplier for parameter #${parameter.index} has returned a default value of type ${defaultValue::class.simpleName} but a value of type ${expectedType.simpleName} was expected"
        }

        if (parameter.isVarArg && defaultValue is List<*>) {
            //Check if first parameter exists
            requireUser(defaultValue.firstOrNull() != null) {
                "Default value supplier for parameter #${parameter.index} in %s has returned either an empty list or a list with the first element being null"
            }
        }
    }
}