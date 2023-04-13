package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Interface for embed footer icons requested by {@link BaseCommandEvent#getDefaultIconStream()}.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService}, and a {@link ServiceType} of {@link DefaultEmbedFooterIconSupplier}.
 */
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
