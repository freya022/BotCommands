package com.freya02.botcommands.api.commands.application.context.user;

import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class GuildUserEvent extends GlobalUserEvent {
	public GuildUserEvent(BContextImpl context, UserContextInteractionEvent event) {
		super(context, event);

		if (!event.isFromGuild())
			throw new IllegalStateException("Event is not from a Guild");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <br><b>This is always true for this guild-only event</b>
	 */
	@Override
	public boolean isFromGuild() {
		return true;
	}

	/**
	 * The {@link Guild} this interaction happened in.
	 * <br>This is not null as this object is not constructed if the interaction isn't in a Guild.
	 *
	 * @return The {@link Guild}
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull
	@Override
	public Guild getGuild() {
		return super.getGuild();
	}
}
