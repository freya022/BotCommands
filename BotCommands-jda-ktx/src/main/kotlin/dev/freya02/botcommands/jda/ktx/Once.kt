package dev.freya02.botcommands.jda.ktx

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.utils.Once

/**
 * Same as [JDA.listenOnce] but with a reified type parameter.
 */
inline fun <reified E : GenericEvent> JDA.listenOnce(): Once.Builder<E> = listenOnce(E::class.java)
