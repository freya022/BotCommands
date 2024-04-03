package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.service.annotations.Condition

/**
 * Defines custom conditions used while checking service instantiability.
 *
 * Whether the service creation failure throws or not, is defined by [Condition.fail],
 * or if a dependent service requires it.
 *
 * **Requirement:** A no-arg constructor must exist, or must be a Kotlin `object`.
 *
 * Example:
 * ```kt
 * // Referenced by @RequireProfile
 * object ProfileChecker : CustomConditionChecker<RequireProfile> {
 *     // Let's just say it's loaded from the config
 *     private val currentProfile = Profile.DEV
 *
 *     override val annotationType = RequireProfile::class.java
 *
 *     override fun checkServiceAvailability(
 *         context: BContext,
 *         checkedClass: Class<*>,
 *         annotation: RequireProfile
 *     ): String? {
 *         if (annotation.profile != currentProfile) {
 *             return "Profile ${annotation.profile} is required, current profile is $currentProfile"
 *         }
 *
 *         return null
 *     }
 * }
 * ```
 *
 * @see Condition @Condition
 */
interface CustomConditionChecker<A : Annotation> {
    /**
     * The condition annotation processed by this condition checker.
     *
     * This must be the annotation which is meta-annotated with [Condition],
     * with its [type][Condition.type] being the checker implementation.
     */
    val annotationType: Class<A>

    /**
     * @param serviceContainer The service container for this context
     * @param checkedClass     The primary type of the service being created,
     *                         the class being instantiated for services, or the return type for service factories
     * @param annotation       The condition annotation which triggered this check
     *
     * @return An error string if the service is not instantiable, `null` otherwise
     */
    fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: A): String?
}