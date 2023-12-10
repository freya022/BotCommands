package io.github.freya022.wiki.java.switches;

import io.github.freya022.botcommands.api.core.service.annotations.Condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

// --8<-- [start:dev_command_annotated_condition-annotation-java]
// Same targets as service annotations
@Target({ElementType.TYPE, ElementType.METHOD})
// The implementation of our CustomConditionChecker
@Condition(type = DevCommandChecker.class)
public @interface DevCommand { }
// --8<-- [end:dev_command_annotated_condition-annotation-java]
