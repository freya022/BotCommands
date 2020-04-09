package com.freya02.botcommands.annotation;

import com.freya02.botcommands.CooldownScope;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JdaSubcommand {
    /** Primary name of the command, <b>must not contain any spaces</b>
     * @return Name of the command
     */
    String name();
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

    /** Required role of the user
     * @return Required role of the user
     */
    String requiredRole() default "";

    /** Cooldown time <b>in milliseconds</b> before the command can be used again in the scope specified by {@linkplain #cooldownScope()}
     * @return Cooldown time
     */
    int cooldown() default 0;
    /** Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD}
     * @return Scope of the cooldown
     */
    CooldownScope cooldownScope() default CooldownScope.USER;
}
