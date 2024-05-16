package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import kotlin.reflect.KClass

internal interface CommandAutoBuilder {
    val serviceContainer: ServiceContainer
    val optionAnnotation: KClass<out Annotation>
}

