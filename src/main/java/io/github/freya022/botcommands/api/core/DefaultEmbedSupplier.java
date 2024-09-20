package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker;
import io.github.freya022.botcommands.api.core.service.ServiceContainer;
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Interface for embeds requested by {@link BaseCommandEvent#getDefaultEmbed()}, aiming to reduce boilerplate.
 * <br>This embed is also used in the default help command.
 *
 * <p>Returns an empty {@link EmbedBuilder} by default.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface DefaultEmbedSupplier {
    @Configuration
    @BConfiguration
    class DefaultProvider {
        @Bean
        @ConditionalOnMissingBean(DefaultEmbedSupplier.class)
        @BService
        @ConditionalService(DefaultEmbedSupplier.DefaultProvider.MissingServiceCondition.class)
        public static DefaultEmbedSupplier defaultEmbedSupplier() {
            return EmbedBuilder::new;
        }

        public static class MissingServiceCondition implements ConditionalServiceChecker {
            @Nullable
            @Override
            public String checkServiceAvailability(@NotNull ServiceContainer serviceContainer, @NotNull Class<?> checkedClass) {
                final var types = serviceContainer.getInterfacedServiceTypes(DefaultEmbedSupplier.class);
                if (!types.isEmpty())
                    return "Using user-provided " + DefaultEmbedSupplier.class.getSimpleName();

                return null;
            }
        }
    }

    @NotNull
    EmbedBuilder get();
}
