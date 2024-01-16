package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariation;
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor;
import kotlin.reflect.KParameter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Resolver
public class GuildResolver
		extends ClassParameterResolver<GuildResolver, Guild>
		implements TextParameterResolver<GuildResolver, Guild>,
		           SlashParameterResolver<GuildResolver, Guild>,
		           ComponentParameterResolver<GuildResolver, Guild> {

	public GuildResolver() {
		super(Guild.class);
	}

	@Override
	@Nullable
	public Guild resolve(@NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return resolveGuild(event.getJDA(), args[0]);
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\d+)");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "1234";
	}

	@NotNull
	@Override
	public String getHelpExample(@NotNull KParameter parameter, @NotNull BaseCommandEvent event, boolean isID) {
		return event.getGuild().getId();
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public Guild resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return resolveGuild(event.getJDA(), optionMapping.getAsString());
	}

	@Override
	@Nullable
	public Guild resolve(@NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return resolveGuild(event.getJDA(), arg);
	}

	@Nullable
	private Guild resolveGuild(JDA jda, String arg) {
		return jda.getGuildById(arg);
	}
}
