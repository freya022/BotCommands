package io.github.freya022.wiki.commands.slash

import io.github.freya022.bot.config.Config
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.wiki.switches.wiki.WikiLanguage

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:tag_interfaced_condition-kotlin]
@Command
@ConditionalService(TagCommand.FeatureCheck::class) // Only create the command if this passes
class TagCommand {
    /* */

    object FeatureCheck : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
            val config = context.getService<Config>() // Suppose this is your configuration
            if (!config.enableTags) {
                return "Tags are disabled in the configuration" // Do not allow the tag command!
            }
            return null // No error message, allow the tag command!
        }
    }
}
// --8<-- [end:tag_interfaced_condition-kotlin]