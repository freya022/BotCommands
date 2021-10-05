package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.prefixed.annotations.JdaTextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * <p>Extend this class on the class used for a <b>command or a subcommand</b></p>
 * <p>You also need to use the {@linkplain JdaTextCommand @JdaCommand} annotation in order to register a command with {@linkplain CommandsBuilder}</p>
 */
public abstract class TextCommand {
	private EmbedBuilder helpBuilder = null;

	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal 'help' command</p>
	 * <p>The 'help' command will automatically set the embed title to be "<code>Command '[command_name]'</code>" but can be overridden</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@linkplain EmbedBuilder#setDescription(CharSequence)}</b></p>
	 * @return The EmbedBuilder to use as a detailed description
	 */
	@Nullable
	public Consumer<EmbedBuilder> getDetailedDescription() { return null; }

//	public synchronized void showHelp(BaseCommandEvent event) {
//		final Consumer<BaseCommandEvent> helpConsumer = event.getContext().getHelpConsumer();
//		if (helpConsumer != null) {
//			helpConsumer.accept(event);
//
//			return;
//		}
//
//		if (helpBuilder == null) {
//			helpBuilder = com.freya02.botcommands.internal.prefixed.Utils.generateHelp(this, event);
//
//			final Consumer<EmbedBuilder> helpBuilderConsumer = event.getContext().getHelpBuilderConsumer();
//			if (helpBuilderConsumer != null) helpBuilderConsumer.accept(helpBuilder);
//		}
//
//		helpBuilder.setTimestamp(Instant.now());
//		helpBuilder.setFooter("Arguments in black boxes `foo` are obligatory, but arguments in brackets [bar] are optional");
//
//		final Member member = event.getMember();
//		helpBuilder.setColor(member.getColorRaw());
//
//		event.respond(helpBuilder.build()).queue(null, event.failureReporter("Unable to send help message"));
//	}
}
