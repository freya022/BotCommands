package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.internal.core.service.ConditionalOnMissingServiceChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * Condition which enables the annotated service or service factory if no other instance of the specified types exist.
 *
 * For Spring users, this is the same as [@ConditionalOnMissingBean][ConditionalOnMissingBean].
 */
@MustBeDocumented
@Condition(ConditionalOnMissingServiceChecker::class)
@ConditionalOnMissingBean
annotation class ConditionalOnMissingService(
    @get:AliasFor(attribute = "value", annotation = ConditionalOnMissingBean::class)
    vararg val value: KClass<*> = [],
)
