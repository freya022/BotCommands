package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

/**
 * Base class for application commands (slash / context commands).
 * <br>Every application command has to inherit this class.
 * <p>
 * <b>Note: </b>You are able to get a BContext by putting it in your constructor, this works with <a href="https://freya022.github.io/BotCommands-Wiki/writing-extensions/Constructor-injection/" target="_blank">constructor injection</a>.
 *
 * @see JDASlashCommand
 * @see JDAMessageCommand
 * @see JDAUserCommand
 * @see <a href="https://freya022.github.io/BotCommands-Wiki/writing-extensions/Constructor-injection/" target="_blank">Wiki on Constructor injection</a>
 */
public abstract class ApplicationCommand implements GuildApplicationSettings {}