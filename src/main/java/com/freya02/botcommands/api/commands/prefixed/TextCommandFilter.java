package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder;
import com.freya02.botcommands.api.core.CooldownService;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Filters text commands, any filter that returns {@code false} prevents the command from executing.
 *
 * <p>Filters are tested right before the command gets executed (i.e., after the permissions/cooldown... were checked).
 *
 * <p><b>Note:</b> this runs on every {@link TextCommandBuilder#variation(KFunction) command variation}.
 *
 * <p><b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 *
 * @see #isAccepted(MessageReceivedEvent, TextCommandInfo, String)
 * @see CooldownService
 */
@InterfacedService(acceptMultiple = true)
public interface TextCommandFilter {
	/**
	 * Returns whether the command should be accepted or not.
	 *
	 * @return {@code true} if the command can run, {@code false} otherwise
	 *
	 * @see TextCommandFilter
	 */
	boolean isAccepted(@NotNull MessageReceivedEvent event, @NotNull TextCommandInfo commandInfo, @NotNull String args);
}
