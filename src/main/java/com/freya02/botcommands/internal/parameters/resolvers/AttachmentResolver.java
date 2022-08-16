package com.freya02.botcommands.internal.parameters.resolvers;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.parameters.ParameterResolver;
import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttachmentResolver
		extends ParameterResolver<AttachmentResolver, Attachment>
		implements SlashParameterResolver<AttachmentResolver, Attachment> {

	public AttachmentResolver() {
		super(ParameterType.ofClass(Attachment.class));
	}

	@Override
	@NotNull
	public OptionType getOptionType() {
		return OptionType.ATTACHMENT;
	}

	@Override
	@Nullable
	public Attachment resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping) {
		return optionMapping.getAsAttachment();
	}
}
