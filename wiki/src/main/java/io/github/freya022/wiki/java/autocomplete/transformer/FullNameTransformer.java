package io.github.freya022.wiki.java.autocomplete.transformer;

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:autocomplete_transformer-java]
@BService
public class FullNameTransformer implements AutocompleteTransformer<FullName> {
    @Override
    public @NotNull Class<FullName> getElementType() {
        return FullName.class;
    }

    @NotNull
    @Override
    public Command.Choice apply(@NotNull FullName fullName) {
        return new Command.Choice(
                "%s %s".formatted(fullName.firstName(), fullName.secondName()),
                "%s|%s".formatted(fullName, fullName.secondName())
        );
    }
}
// --8<-- [end:autocomplete_transformer-java]
