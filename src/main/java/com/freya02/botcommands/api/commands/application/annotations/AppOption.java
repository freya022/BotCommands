package com.freya02.botcommands.api.commands.application.annotations;

import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.slash.annotations.*;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CompositeKey;
import com.freya02.botcommands.internal.annotations.DiscordNamePattern;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a application command parameter is supplied from Discord, <i>i.e. it is <b>not</b> a custom parameter</i>
 * <br>This also can set name and description of {@linkplain JDASlashCommand slash commands} parameters
 * <p>
 * {@linkplain #name()} is optional if the parameter name is available (add -parameters to your java compiler)
 *
 * @see Optional @Optional
 * @see Nullable @Nullable (same as @Optional but better)
 * @see LongRange @LongRange
 * @see DoubleRange @DoubleRange
 * @see Length @Length
 * @see ChannelTypes @ChannelTypes
 * @see AutocompleteHandler @AutocompleteHandler
 * @see CompositeKey @CompositeKey
 * @see VarArgs @VarArgs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface AppOption { //TODO separate this into specialised options, why use AppOption for things that don't support descriptions / autocomplete ?
	/**
	 * Name of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <br>This is optional if the parameter name is found (using -parameters on javac)
	 * <br>This can be a localization property
	 *
	 * @return Name of the option
	 */
	@DiscordNamePattern
	String name() default "";

	/**
	 * Description of the option, must follow the Discord specifications, see {@linkplain OptionData#OptionData(OptionType, String, String)} for details
	 * <p>
	 * <br>This is optional if the parameter is not a slash command parameter, <b>otherwise it is defaulted to <code>"No Description"</code></b>
	 * <br>This can be a localization property
	 *
	 * @return Description of the option
	 */
	String description() default "";

	/**
	 * Name of the autocomplete handler, must match a method annotated with {@link AutocompleteHandler} with the same name in it
	 *
	 * @see CompositeKey
	 */
	String autocomplete() default "";
}
