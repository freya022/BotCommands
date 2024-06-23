package io.github.freya022.botcommands.api.commands.application.slash.autocomplete;

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to transform autocomplete results into choices.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @param <E> Type of the List's elements
 *
 * @see SlashOption#autocomplete()
 * @see AutocompleteHandler @AutocompleteHandler
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
public interface AutocompleteTransformer<E> {
    @NotNull
    Class<E> getElementType();

    @NotNull
    Command.Choice apply(@NotNull E e);
}
