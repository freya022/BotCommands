package com.freya02.botcommands.api.core.conditions

import com.freya02.botcommands.api.core.service.annotations.Condition
import com.freya02.botcommands.internal.conditions.RequiredIntentsChecker
import net.dv8tion.jda.api.requests.GatewayIntent

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(RequiredIntentsChecker::class, fail = false)
annotation class RequiredIntents(vararg val intents: GatewayIntent)
