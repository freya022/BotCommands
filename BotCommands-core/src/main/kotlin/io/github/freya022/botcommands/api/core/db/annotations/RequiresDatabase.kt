package io.github.freya022.botcommands.api.core.db.annotations

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

/**
 * Prevents usage of the annotated service if the required [ConnectionSupplier] is not found.
 *
 * @see ConnectionSupplier
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Dependencies(ConnectionSupplier::class)
@ConditionalOnBean(ConnectionSupplier::class)
annotation class RequiresDatabase