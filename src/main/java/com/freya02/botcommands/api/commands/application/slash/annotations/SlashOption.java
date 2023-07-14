package com.freya02.botcommands.api.commands.application.slash.annotations;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.GuildApplicationSettings;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;
import com.freya02.botcommands.api.core.config.BApplicationConfigBuilder;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.annotations.DiscordNamePattern;
import kotlin.jvm.functions.Function1;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.entities.Guild;
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
 * Sets a parameter as a slash command option from Discord.
 *
 * <p>
 * {@link #name()} is optional if the parameter name is available (add {@code -parameters} to your java compiler)
 *
 * @see <a href="https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/" target="_blank">Wiki about inferred option names</a>
 *
 * @see Optional @Optional
 * @see Nullable @Nullable (same as @Optional but better)
 * @see LongRange @LongRange
 * @see DoubleRange @DoubleRange
 * @see Length @Length
 * @see ChannelTypes @ChannelTypes
 * @see AutocompleteHandler @AutocompleteHandler
 * @see VarArgs @VarArgs
 *
 * @see SlashCommandBuilder#option(String, String, Function1) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface SlashOption {
	/**
	 * Name of the option.
	 * <br>Must follow the Discord specifications,
	 * see {@link OptionData#OptionData(OptionType, String, String) the OptionData constructor} for details.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how options are mapped.
	 *
	 * <p>This is optional if the parameter name is found,
	 * see <a href="https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/" target="_blank">the wiki</a> for more details.
	 *
	 * @return Name of the option
	 */
	@DiscordNamePattern
	String name() default "";

	/**
	 * Description of the option.
	 * <br>Must follow the Discord specifications, see {@link OptionData#OptionData(OptionType, String, String)} for details.
	 *
	 * <p>
	 * If this description is omitted, a default localization is
	 * searched in {@link BApplicationConfigBuilder#addLocalizations(String, DiscordLocale...) the command localization bundles}
	 * using the root locale, for example: {@code MyCommands.json}.
	 * <br>If none is found then it is defaulted to {@code "No Description"}.
	 *
	 * <p>
	 * This can be localized, see {@link LocalizationFunction} on how options are mapped, example: {@code ban.options.user.description}.
	 * <br>This is optional if the parameter is not a slash command parameter.
	 *
	 * @return Description of the option
	 *
	 * @see SlashCommandOptionBuilder#setDescription(String) DSL equivalent
	 */
	String description() default "";

	/**
	 * Enables using choices from {@link SlashParameterResolver#getPredefinedChoices(Guild)}.
	 *
	 * <p><b>Note:</b> Predefined choices can still be overridden by {@link GuildApplicationSettings#getOptionChoices(Guild, CommandPath, String)}.
	 *
	 * @return {@code true} to enable using choices from {@link SlashParameterResolver#getPredefinedChoices(Guild)}
	 *
	 * @see SlashCommandOptionBuilder#setUsePredefinedChoices(boolean) DSL equivalent
	 */
	boolean usePredefinedChoices() default false;

	/**
	 * Name of the autocomplete handler.
	 * <br>Must match a method annotated with {@link AutocompleteHandler} with the same name in it
	 *
	 * @see SlashCommandOptionBuilder#autocompleteReference(String) DSL equivalent
	 * @see SlashCommandOptionBuilder#autocomplete(String, KFunction, Function1) Declaring an autocomplete handler using the DSL
	 */
	String autocomplete() default "";
}
