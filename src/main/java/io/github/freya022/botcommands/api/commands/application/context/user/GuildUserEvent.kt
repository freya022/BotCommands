package io.github.freya022.botcommands.api.commands.application.context.user;

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit;
import io.github.freya022.botcommands.api.core.BContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class GuildUserEvent extends GlobalUserEvent {
	public GuildUserEvent(BContext context, UserContextInteractionEvent event, CancellableRateLimit cancellableRateLimit) {
		super(context, event, cancellableRateLimit);

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
