package com.freya02.botcommands.annotation;

import com.freya02.botcommands.Command;
import com.freya02.botcommands.CooldownScope;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Required annotation for bot commands, see all possible options */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JdaCommand {
    /** Primary name of the command, <b>must not contain any spaces</b> */
    String name();
    /** Secondary names of the command, <b>must not contain any spaces</b> */
    String[] aliases() default {};
    /** Short description of the command, it is displayed in the help command */
    String description() default "No description";

    /** Required {@linkplain Permission permissions} of the bot */
    Permission[] botPermissions() default {};
    /** Required {@linkplain Permission permissions} of the user */
    Permission[] userPermissions() default {};

    /** Required role of the user */
    String requiredRole() default "";

    /** Cooldown time <b>in milliseconds</b> before the command can be used again in the scope specified by {@linkplain #cooldownScope()} */
    int cooldown() default 0;
    /** Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD} */
    CooldownScope cooldownScope() default CooldownScope.USER;

    /** Name of the category the command should be in */
    String category() default "No category";

    /** Classes of the subcommands for this command, they must extend {@linkplain Command} and have the annotation {@linkplain JdaSubcommand} */
    Class<? extends Command>[] subcommands() default {};
}
