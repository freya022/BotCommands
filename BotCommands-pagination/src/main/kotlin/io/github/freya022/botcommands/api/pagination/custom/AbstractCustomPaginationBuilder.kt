package io.github.freya022.botcommands.api.pagination.custom

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.AbstractPagination
import io.github.freya022.botcommands.api.pagination.AbstractPaginationBuilder

/**
 * Most basic paginator builder.
 *
 * @param T Type of the pagination builder
 * @param R Type of the built pagination
 */
abstract class AbstractCustomPaginationBuilder<T : AbstractPaginationBuilder<T, R>, R : AbstractPagination<R>>(
    context: BContext
) : AbstractPaginationBuilder<T, R>(context)
