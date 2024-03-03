package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.bot.config.Config;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker;
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:tag_interfaced_condition-java]
@Command
@ConditionalService(TagCommand.FeatureCheck.class) // Only create the command if this passes
public class TagCommand {
    /* */

    public static class FeatureCheck implements ConditionalServiceChecker {
        @Nullable
        @Override
        public String checkServiceAvailability(@NotNull BContext context, @NotNull Class<?> checkedClass) {
            final var config = context.getService(Config.class); // Suppose this is your configuration
            if (!config.areTagsEnabled()) {
                return "Tags are disabled in the configuration"; // Do not allow the tag command!
            }
            return null; // No error message, allow the tag command!
        }
    }
}
// --8<-- [start:tag_interfaced_condition-java]
