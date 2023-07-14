package com.freya02.botcommands.api.commands.annotations;

import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an optional parameter annotated with {@link SlashOption @SlashOption} or {@link TextOption @TextOption}
 * <p>You can also use the {@link Nullable @Nullable} annotation to represent an optional parameter while benefiting from static analysis
 *
 * <p><br><b>For regex commands: Consider this annotation as experimental, you might have errors if your command is deemed too complex, for examples having strings in wrong places</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional {}