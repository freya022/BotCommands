package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for embeds requested by {@link BaseCommandEvent#getDefaultEmbed()}, aiming to reduce boilerplate.
 * <br>This embed is also used in the default help command.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService}, and a {@link ServiceType} of {@link DefaultEmbedSupplier}.
 */
public interface DefaultEmbedSupplier {
	class Default implements DefaultEmbedSupplier {
		@NotNull
		@Override
		public EmbedBuilder get() {
			return new EmbedBuilder();
		}
	}

	@NotNull
	EmbedBuilder get();
}
