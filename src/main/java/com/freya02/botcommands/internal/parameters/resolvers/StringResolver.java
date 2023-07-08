package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.service.annotations.Resolver;
import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import com.freya02.botcommands.internal.modals.ModalHandlerInfo;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Resolver
public class StringResolver
		extends ParameterResolver<StringResolver, String>
		implements RegexParameterResolver<StringResolver, String>,
		           QuotableRegexParameterResolver,
		           SlashParameterResolver<StringResolver, String>,
		           ComponentParameterResolver<StringResolver, String>,
		           ModalParameterResolver<StringResolver, String> {

	public StringResolver() {
		super(String.class);
	}

	@Override
	@Nullable
	public String resolve(@NotNull BContext context, @NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
		return args[0];
	}

	@Override
	@NotNull
	public Pattern getPattern() {
		return Pattern.compile("(\\X+)");
	}

	@Override
	@NotNull
	public Pattern getQuotedPattern() {
		return Pattern.compile("\"(\\X+)\"");
	}

	@Override
	@NotNull
	public String getTestExample() {
		return "foobar";
	}

	@NotNull
	@Override
	public String getHelpExample(boolean isID) {
		return "foo bar";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public String resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsString();
	}

	@Override
	@Nullable
	public String resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return arg;
	}

	@Override
	@Nullable
	public String resolve(@NotNull BContext context, @NotNull ModalHandlerInfo info, @NotNull ModalInteractionEvent event, @NotNull ModalMapping modalMapping) {
		return modalMapping.getAsString();
	}
}