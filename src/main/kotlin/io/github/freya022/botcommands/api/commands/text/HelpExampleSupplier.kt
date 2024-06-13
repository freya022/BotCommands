package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.text.annotations.ID
import kotlin.reflect.KParameter

interface HelpExampleSupplier {
    /**
     * Returns a help example for this parameter.
     *
     * **Tip:** You may use the event as a way to get sample data (such as getting the member, channel, roles, etc...).
     *
     * @param parameter The [parameter][KParameter] of the command being shown in the help content
     * @param event     The event of the command that triggered help content to be displayed
     * @param isID      Whether this option was [marked as being an ID][ID]
     */
    fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String
}