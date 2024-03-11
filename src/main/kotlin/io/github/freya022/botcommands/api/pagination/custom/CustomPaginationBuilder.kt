package io.github.freya022.botcommands.api.pagination.custom

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.pagination.Paginators

/**
 * Builds a [CustomPagination].
 *
 * @see Paginators.customPagination
 * @see Paginators.singlePage
 */
class CustomPaginationBuilder internal constructor(
    context: BContext,
    val maxPages: Int,
    val pageEditor: CustomPageEditor<CustomPagination>
) : AbstractCustomPaginationBuilder<CustomPaginationBuilder, CustomPagination>(context) {
    override fun build(): CustomPagination = CustomPagination(context, this)
}
