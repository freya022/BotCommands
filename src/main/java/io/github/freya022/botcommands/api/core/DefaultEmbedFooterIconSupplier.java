package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker;
import io.github.freya022.botcommands.api.core.service.ServiceContainer;
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

/**
 * Interface for embed footer icons requested by {@link BaseCommandEvent#getDefaultIconStream()}.
 *
 * <p>Returns {@code null} by default.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface DefaultEmbedFooterIconSupplier {
    @Configuration
    @BConfiguration
    class DefaultProvider {
        @Bean
        @ConditionalOnMissingBean(DefaultEmbedFooterIconSupplier.class)
        @BService
        @ConditionalService(MissingServiceCondition.class)
        public static DefaultEmbedFooterIconSupplier defaultEmbedFooterIconSupplier() {
            return () -> null;
        }

        public static class MissingServiceCondition implements ConditionalServiceChecker {
            @Nullable
            @Override
            public String checkServiceAvailability(@NotNull ServiceContainer serviceContainer, @NotNull Class<?> checkedClass) {
                final var types = serviceContainer.getInterfacedServiceTypes(DefaultEmbedFooterIconSupplier.class);
                if (!types.isEmpty())
                    return "Using user-provided " + DefaultEmbedFooterIconSupplier.class.getSimpleName();

                return null;
            }
        }
    }

    @Nullable
    InputStream get();
}
