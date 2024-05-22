package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import io.github.freya022.botcommands.api.core.annotations.Handler;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.Collection;
import java.util.List;

@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:word_autocomplete-java]
@Handler // Required by the AutocompleteHandler annotation, can be replaced with @Command
public class SlashWordAutocomplete extends ApplicationCommand {
    // https://en.wikipedia.org/wiki/Dolch_word_list#Dolch_list:_Nouns
    // but 30 words
    private static final List<String> WORDS = List.of(
            "apple", "baby", "back", "ball", "bear", "bed", "bell", "bird", "birthday", "boat",
            "box", "boy", "bread", "brother", "cake", "car", "cat", "chair", "chicken", "children",
            "Christmas", "coat", "corn", "cow", "day", "dog", "doll", "door", "duck", "egg"
    );
    public static final String WORD_AUTOCOMPLETE_NAME = "SlashWord: word";

    // You can also make this return a collection of Choice, see the annotation docs
    @AutocompleteHandler(WORD_AUTOCOMPLETE_NAME)
    public Collection<String> onWordAutocomplete(CommandAutoCompleteInteractionEvent event) {
        // Here you would typically filter the words based on what the user inputs,
        // but it is already done when you return a Collection<String>
        return WORDS;
    }
}
// --8<-- [end:word_autocomplete-java]
