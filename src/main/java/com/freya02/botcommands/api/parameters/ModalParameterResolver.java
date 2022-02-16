package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.modals.ModalHandlerInfo;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModalParameterResolver {
	/**
	 * Returns a resolved object for this {@link OptionMapping}
	 *
	 * @param context      The {@link BContext} of this bot
	 * @param info         The modal handler info of the command being executed
	 * @param event        The event of this modal interaction
	 * @param modalMapping The {@link ModalMapping} to be resolved
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull ModalHandlerInfo info, @NotNull ModalInteractionEvent event, @NotNull ModalMapping modalMapping);
}
