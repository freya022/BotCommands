package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Retrieves the bucket key given the execution context.
 *
 * You can use the provided parameters to create the key.
 */
interface BucketKeySupplier<K> {
    fun getKey(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): K

    fun getKey(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): K

    fun getKey(context: BContext, event: GenericComponentInteractionCreateEvent): K
}