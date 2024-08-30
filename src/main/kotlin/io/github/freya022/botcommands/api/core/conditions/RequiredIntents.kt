package io.github.freya022.botcommands.api.core.conditions

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.core.conditions.RequiredIntentsChecker
import io.github.freya022.botcommands.internal.core.conditions.SpringRequiredIntentsChecker
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.Conditional

/**
 * Prevents usage of the annotated service if the required intents are not present in [JDAService.intents].
 *
 * **Note:** If you are using Spring, this checks against the `jda.intents` property.
 *
 * @see JDAService
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(RequiredIntentsChecker::class, fail = false)
@Conditional(SpringRequiredIntentsChecker::class)
annotation class RequiredIntents(@get:JvmName("value") vararg val intents: GatewayIntent)
