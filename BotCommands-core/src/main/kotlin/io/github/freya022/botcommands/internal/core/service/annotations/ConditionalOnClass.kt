package io.github.freya022.botcommands.internal.core.service.annotations

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

/** **INTERNAL** */
@Condition(ConditionalOnClassChecker::class)
annotation class ConditionalOnClass(
    vararg val value: KClass<out Any>,
)

internal object ConditionalOnClassChecker : CustomConditionChecker<ConditionalOnClass> {

    override val annotationType: Class<ConditionalOnClass> get() = ConditionalOnClass::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: ConditionalOnClass,
    ): String? {
        try {
            // Throws if any of the classes is missing
            annotation.value
        } catch (e: TypeNotPresentException) {
            val cause = e.cause
            return if (cause is ClassNotFoundException) {
                "Class ${cause.message} required by ${checkedClass.shortQualifiedName} is not present"
            } else {
                // Cause isn't what we expected, log it
                KotlinLogging.logger(checkedClass.name).debug(e) { "Error loading conditional on class of ${checkedClass.shortQualifiedName}" }
                "One or more class required by ${checkedClass.shortQualifiedName} could not be loaded"
            }
        }

        return null
    }
}
