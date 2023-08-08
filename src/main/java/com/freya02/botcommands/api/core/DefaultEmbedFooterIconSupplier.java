package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Interface for embed footer icons requested by {@link BaseCommandEvent#getDefaultIconStream()}.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface DefaultEmbedFooterIconSupplier {
	class Default implements DefaultEmbedFooterIconSupplier {
		@Nullable
		@Override
		public InputStream get() {
			return null;
		}
	}

	@Nullable
	InputStream get();
}
