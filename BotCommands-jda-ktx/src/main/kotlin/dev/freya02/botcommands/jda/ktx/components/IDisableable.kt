package dev.freya02.botcommands.jda.ktx.components

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import net.dv8tion.jda.api.interactions.components.LayoutComponent

/**
 * Returns a list containing the results of disabling/enabling each component in the original collection.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Iterable<T>.withDisabled(disabled: Boolean) = map {
    it.withDisabled(disabled)
}

/**
 * Returns a list containing the results of disabling each component in the original collection.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Iterable<T>.asDisabled() = withDisabled(true)

/**
 * Returns a list containing the results of enabling each component in the original collection.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Iterable<T>.asEnabled() = withDisabled(false)

/**
 * Returns a sequence containing the results of disabling/enabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Sequence<T>.withDisabled(disabled: Boolean) = map {
    it.withDisabled(disabled)
}

/**
 * Returns a sequence containing the results of disabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Sequence<T>.asDisabled() = withDisabled(true)

/**
 * Returns a sequence containing the results of enabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
@ReplaceJdaKtx
fun <T : LayoutComponent> Sequence<T>.asEnabled() = withDisabled(false)
