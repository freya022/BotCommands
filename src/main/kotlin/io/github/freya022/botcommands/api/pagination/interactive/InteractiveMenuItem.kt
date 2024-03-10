package io.github.freya022.botcommands.api.pagination.interactive

data class InteractiveMenuItem<R : BasicInteractiveMenu<R>>(
    val content: SelectContent,
    val maxPages: Int,
    val supplier: InteractiveMenuPageEditor<R>
) 