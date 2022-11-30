package com.freya02.botcommands.api.components.builder.selects;

import com.freya02.botcommands.api.components.SelectionConsumer;
import com.freya02.botcommands.api.components.builder.ComponentBuilder;
import com.freya02.botcommands.api.components.builder.LambdaComponentBuilder;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface LambdaSelectionMenuBuilder<T extends LambdaSelectionMenuBuilder<T, E>, E extends GenericSelectMenuInteractionEvent<?, ?>> extends ComponentBuilder<T>,
                                                                                                                                                   LambdaComponentBuilder<T> {

	@NotNull
	SelectionConsumer<E> getConsumer();
}
