package dev.freya02.botcommands.jda.ktx.ranges

import net.dv8tion.jda.api.components.selections.SelectMenu

/**
 * The minimum and maximum amount of values a user can select, must not exceed [SelectMenu.OPTIONS_MAX_AMOUNT].
 */
fun <B : SelectMenu.Builder<*, B>> SelectMenu.Builder<*, B>.setRequiredRange(range: IntRange): B = setRequiredRange(range.first, range.last)
