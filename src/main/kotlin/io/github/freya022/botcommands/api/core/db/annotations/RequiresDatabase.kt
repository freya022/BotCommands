package io.github.freya022.botcommands.api.core.db.annotations

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

//TODO when BC supports reading inherited annotations,
// add @Dependencies(ConnectionSupplier::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@ConditionalOnBean(ConnectionSupplier::class)
annotation class RequiresDatabase