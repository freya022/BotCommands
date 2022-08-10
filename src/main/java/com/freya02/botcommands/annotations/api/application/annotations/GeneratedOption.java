package com.freya02.botcommands.annotations.api.application.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO docs
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface GeneratedOption { //TODO separate this into specialised options, why use AppOption for things that don't support descriptions / autocomplete ?
}