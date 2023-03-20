package com.freya02.botcommands.api.application.annotations;

import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.slash.annotations.*;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CompositeKey;
import com.freya02.botcommands.api.builder.ApplicationCommandsBuilder;
import com.freya02.botcommands.internal.annotations.DiscordNamePattern;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify an application command parameter is supplied from Discord.
 * <br>This also can set name and description of {@linkplain JDASlashCommand slash commands} parameters.
 * <p>
 * {@linkplain #name()} is optional if the parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional @Optional
 * @see Nullable @Nullable (same as @Optional but better)
 * @see LongRange @LongRange
 * @see DoubleRange @DoubleRange
 * @see ChannelTypes @ChannelTypes
 * @see AutocompletionHandler @AutocompletionHandler
 * @see CompositeKey @CompositeKey
 * @see VarArgs @VarArgs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface AppOption {
	/**
	 * Name of the option, must follow the Discord specifications, see {@link OptionData#OptionData(OptionType, String, String)} for details.
	 *
	 * <p>
	 * This can be a localization property, see {@link LocalizationFunction} on how options are mapped.
	 * <br>This is optional if the parameter name is found, see <a href="https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/" target="_blank">the wiki</a> for more details.
	 *
	 * @return Name of the option
	 */
	@DiscordNamePattern
	String name() default "";

	/**
	 * Description of the option, must follow the Discord specifications, see {@link OptionData#OptionData(OptionType, String, String)} for details.
	 *
	 * <p>
	 * If this description is omitted, a default localization is searched in {@link ApplicationCommandsBuilder#addLocalizations(String, DiscordLocale...) the command localization bundles}
	 * using the root locale (i.e. no prefix after the bundle name).
	 * <br>If none is found then it is defaulted to <code>"No Description"</code>.
	 *
	 * <p>
	 * This can be a localization property, see {@link LocalizationFunction} on how options are mapped.
	 * <br>This is optional if the parameter is not a slash command parameter.
	 *
	 * @return Description of the option
	 */
	String description() default "";

	/**
	 * Name of the autocompletion handler, must match a method annotated with {@link AutocompletionHandler} with the same name in it
	 *
	 * @see CompositeKey
	 */
	String autocomplete() default "";
}
