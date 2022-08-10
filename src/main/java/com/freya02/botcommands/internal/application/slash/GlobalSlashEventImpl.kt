package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.internal.BContextImpl;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

public class GlobalSlashEventImpl extends GlobalSlashEvent {
	private final BContext context;

	public GlobalSlashEventImpl(@NotNull BContextImpl context, @NotNull KFunction<?> function, @NotNull SlashCommandInteractionEvent event) {
		super(context, function, event.getJDA(), event.getResponseNumber(), (SlashCommandInteractionImpl) event.getInteraction());
		
		this.context = context;
	}

	public BContext getContext() {
		return context;
	}
}
