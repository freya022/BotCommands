package io.github.freya022.botcommands.internal.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.internal.requests.DeferredRestAction

internal inline fun <reified R : Any> JDA.deferredRestAction(noinline valueSupplier: () -> R?, noinline actionSupplier: () -> RestAction<R>): CacheRestAction<R> =
    DeferredRestAction(this, R::class.java, valueSupplier, actionSupplier)

internal val CommandInteraction.uniqueCommandPath: String
    get() = buildString(/* 19 + 8 (average length) */ 27) {
        append(commandIdLong)
        if (subcommandGroup != null)
            append(' ').append(subcommandGroup)
        if (subcommandName != null)
            append(' ').append(subcommandName)
    }