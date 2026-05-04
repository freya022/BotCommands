package io.github.freya022.botcommands.api.core.db.annotations

import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.core.db.RequiresDatabaseChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if the database feature is not registered.
 *
 * @see BDatabaseConfig
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresDatabaseChecker::class)
@ConditionalOnProperty("botcommands.database.enable", matchIfMissing = true)
annotation class RequiresDatabase
