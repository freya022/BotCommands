package dev.freya02.botcommands.jda.ktx.components

import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.tree.ComponentTree

/**
 * Returns a list containing the results of disabling/enabling each component in the original collection.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Component> Iterable<T>.withDisabled(disabled: Boolean): List<T> =
    ComponentTree.of(this.toList()).withDisabled(disabled).components as List<T>

/**
 * Returns a list containing the results of disabling each component in the original collection.
 */
fun <T : Component> Iterable<T>.asDisabled() = withDisabled(true)

/**
 * Returns a list containing the results of enabling each component in the original collection.
 */
fun <T : Component> Iterable<T>.asEnabled() = withDisabled(false)

/**
 * Returns a sequence containing the results of disabling/enabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
fun <T : Component> Sequence<T>.withDisabled(disabled: Boolean) =
    asIterable().withDisabled(disabled).asSequence()

/**
 * Returns a sequence containing the results of disabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
fun <T : Component> Sequence<T>.asDisabled() = withDisabled(true)

/**
 * Returns a sequence containing the results of enabling each component in the original sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
fun <T : Component> Sequence<T>.asEnabled() = withDisabled(false)
