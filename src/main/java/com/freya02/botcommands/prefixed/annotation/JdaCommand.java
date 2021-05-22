package com.freya02.botcommands.prefixed.annotation;

import com.freya02.botcommands.CooldownScope;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation for bot commands, see all possible options
 *
 * @see AddExecutableHelp
 * @see AddSubcommandHelp
 * @see ArgExample
 * @see ArgName
 * @see Executable
 * @see Hidden
 * @see ID
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JdaCommand {
    /** Primary name of the command, <b>must not contain any spaces</b>
     * @return Name of the command
     */
    String name();
    /** Secondary names of the command, <b>must not contain any spaces</b>
     * @return Secondary names of the command
     */
    String[] aliases() default {};
    /** Short description of the command, it is displayed in the help command
     * @return Short description of the command
     */
    String description() default "No description";

    /** Required {@linkplain Permission permissions} of the bot
     * @return Required {@linkplain Permission permissions} of the bot
     */
    Permission[] botPermissions() default {};
    /** Required {@linkplain Permission permissions} of the user
     * @return Required {@linkplain Permission permissions} of the user
     */
    Permission[] userPermissions() default {};

    /** Cooldown time <b>in milliseconds</b> before the command can be used again in the scope specified by {@linkplain #cooldownScope()}
     * @return Cooldown time
     */
    int cooldown() default 0;
    /** Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD}
     * @return Scope of the cooldown
     */
    CooldownScope cooldownScope() default CooldownScope.USER;

    /** Name of the category the command should be in
     * <b>This is ignored in a subcommand</b>
     * @return Name of the category
     */
    String category() default "No category";
}
