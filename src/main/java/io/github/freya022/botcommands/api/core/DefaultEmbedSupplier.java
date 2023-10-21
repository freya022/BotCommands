package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for embeds requested by {@link BaseCommandEvent#getDefaultEmbed()}, aiming to reduce boilerplate.
 * <br>This embed is also used in the default help command.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
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
