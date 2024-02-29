package io.github.freya022.botcommands.api.core.conditions

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.conditions.RequiredIntentsChecker
import net.dv8tion.jda.api.requests.GatewayIntent

/**
 * Prevents usage of the annotated service if the required intents are not present in [JDAService.intents].
 *
 * @see JDAService
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(RequiredIntentsChecker::class, fail = false)
annotation class RequiredIntents(@get:JvmName("value") vararg val intents: GatewayIntent)
