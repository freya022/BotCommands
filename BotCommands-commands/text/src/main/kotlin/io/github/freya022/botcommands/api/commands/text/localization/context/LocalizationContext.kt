package io.github.freya022.botcommands.api.commands.text.localization.context

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.context.PairEntry
import io.github.freya022.botcommands.api.localization.context.localize
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction

/**
 * Sends a localized message to the event's channel.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see MessageChannel.sendMessage
 */
fun BaseCommandEvent.respondLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageCreateAction =
    respond(context.localize(localizationPath, *entries))

/**
 * Replies a localized message to the user's command.
 *
 * @param localizationPath The path of the localization template,
 * prefixed with [localizationPrefix][LocalizationContext.localizationPrefix] unless starting with `/`
 * @param entries          The entries to fill the template with
 *
 * @see MessageChannel.sendMessage
 */
fun BaseCommandEvent.replyLocalized(context: LocalizationContext, localizationPath: String, vararg entries: PairEntry): MessageCreateAction =
    reply(context.localize(localizationPath, *entries))
