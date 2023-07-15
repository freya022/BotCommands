package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to transform autocomplete results into choices.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService}, and a {@link ServiceType} of {@link AutocompleteTransformer}.
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
