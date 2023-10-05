package io.github.freya022.botcommands.api.commands.application.slash;

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit;
import io.github.freya022.botcommands.internal.commands.application.slash.GlobalSlashEventImpl;
import io.github.freya022.botcommands.internal.core.BContextImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class GuildSlashEvent extends GlobalSlashEventImpl {
	public GuildSlashEvent(@NotNull BContextImpl context, @NotNull SlashCommandInteractionEvent event, @NotNull CancellableRateLimit cancellableRateLimit) {
		super(context, event, cancellableRateLimit);

		if (!event.isFromGuild()) throw new IllegalArgumentException("Event is not from a guild");
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

	/**
	 * The {@link Member} who caused this interaction.
	 * <br>This is not null as this object is not constructed if the interaction isn't in a Guild.
	 *
	 * @return The {@link Member}
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull
	@Override
	public Member getMember() {
		return super.getMember();
	}
}
