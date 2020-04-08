package com.freya02.botcommands.annotation;

import com.freya02.botcommands.Command;
import com.freya02.botcommands.CooldownScope;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JdaCommand {
    String name();
    String[] aliases() default {};
    String description() default "No description";

    Permission[] botPermissions() default {};
    Permission[] userPermissions() default {};

    String requiredRole() default "";

    int cooldown() default 0;
    CooldownScope cooldownScope() default CooldownScope.USER;

    String category() default "No category";

    Class<? extends Command>[] subcommands() default {};
}
