package io.github.freya022.botcommands.internal.parameters.resolvers;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.QuotableTextParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo;
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation;
import io.github.freya022.botcommands.internal.components.ComponentDescriptor;
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo;
import kotlin.reflect.KParameter;
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
		extends ClassParameterResolver<StringResolver, String>
		implements QuotableTextParameterResolver<StringResolver, String>,
        SlashParameterResolver<StringResolver, String>,
        ComponentParameterResolver<StringResolver, String>,
        ModalParameterResolver<StringResolver, String> {

	public StringResolver() {
		super(String.class);
	}

	@Override
	@Nullable
	public String resolve(@NotNull TextCommandVariation variation, @NotNull MessageReceivedEvent event, @NotNull String @NotNull [] args) {
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
	public String getHelpExample(@NotNull KParameter parameter, @NotNull BaseCommandEvent event, boolean isID) {
		return "foo bar";
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.STRING;
	}

	@Override
	@Nullable
	public String resolve(@NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsString();
	}

	@Override
	@Nullable
	public String resolve(@NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg) {
		return arg;
	}

	@Override
	@Nullable
	public String resolve(@NotNull ModalHandlerInfo info, @NotNull ModalInteractionEvent event, @NotNull ModalMapping modalMapping) {
		return modalMapping.getAsString();
	}
}