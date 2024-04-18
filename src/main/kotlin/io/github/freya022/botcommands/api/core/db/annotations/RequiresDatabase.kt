package io.github.freya022.botcommands.api.core.db.annotations

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.core.db.RequiresDatabaseChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
// TODO Replace with @Dependencies(ConnectionSupplier::class) when BC supports reading inherited annotations
@Condition(RequiresDatabaseChecker::class)
@ConditionalOnBean(ConnectionSupplier::class)
annotation class RequiresDatabase