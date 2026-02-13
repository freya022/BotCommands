package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.components.RequiresLocalComponentsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresLocalComponentsChecker::class)
@ConditionalOnProperty("botcommands.local-components.enable")
annotation class RequiresLocalComponents
