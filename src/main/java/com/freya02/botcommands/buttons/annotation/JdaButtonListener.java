package com.freya02.botcommands.buttons.annotation;

import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.EmojiOrEmote;
import com.freya02.botcommands.buttons.ButtonId;
import net.dv8tion.jda.api.entities.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a button listener, this has to be the same name as the id given to {@link ButtonId#of(String, Object...)}
 * <p>
 * Supported parameters:
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *     <li>{@linkplain Emote}</li>
 *     <li>{@linkplain EmojiOrEmote}</li>
 *
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *     <li>{@linkplain TextChannel}</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JdaButtonListener {
	/**
	 * Name of the button listener, this is used to find back the handler method after a button has been clicked
	 *
	 * @return Name of the button listener
	 */
	String name();
}