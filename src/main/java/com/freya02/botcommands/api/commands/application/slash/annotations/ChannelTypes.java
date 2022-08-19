package com.freya02.botcommands.api.commands.application.slash.annotations;


import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import net.dv8tion.jda.api.entities.ChannelType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to set the desired channel types for this {@link AppOption app option}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ChannelTypes {
	ChannelType[] value();
}
