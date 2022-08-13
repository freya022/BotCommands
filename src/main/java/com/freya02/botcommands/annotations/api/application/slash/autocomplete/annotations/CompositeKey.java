package com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an application option as one of the values for an autocomplete cache key
 * <br>This would be useful to make an autocomplete result depend on multiple options, instead of only the focused one
 * <p>
 * <br>The focused option will always be in the composite key, whether it's in the autocomplete handler's parameters or not
 *
 * @see AppOption
 * @see AutocompleteHandler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface CompositeKey {}