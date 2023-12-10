package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName;
import io.github.freya022.wiki.java.switches.DevCommand;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;

@ServiceName("slashShutdownJava")
@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:dev_command_annotated_condition-command-java]
@Command
@DevCommand // Our custom condition, this command will only exist if it passes.
public class SlashShutdown {
    /* */
}
// --8<-- [end:dev_command_annotated_condition-command-java]
