package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService
internal class ComponentFilters internal constructor(filters: List<ComponentInteractionFilter<Any>>) {
    private val filters: Map<String, ComponentInteractionFilter<Any>> = filters.associateBy { it.javaClass.name }

    internal fun getFilters(qualifiedNames: Array<out String>): List<ComponentInteractionFilter<*>> {
        return qualifiedNames.map { qualifiedName ->
            filters[qualifiedName] ?: return run {
                logger.warn { "Ignoring component interaction due to missing filter: '$qualifiedName'" }
                INVALID_FILTERS
            }
        }
    }

    internal companion object {
        /**
         * As the component rejection handler is user-managed, we can't make our own filter.
         * So we return this sentinel value, which gets special handling by the listener.
         */
        internal val INVALID_FILTERS: List<ComponentInteractionFilter<*>> = emptyList()
    }
}