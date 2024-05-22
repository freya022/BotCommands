package io.github.freya022.wiki.java.autocomplete.transformer;

import io.github.freya022.wiki.switches.wiki.WikiLanguage;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:full_name_obj-java]
public record FullName(String firstName, String secondName) { }
// --8<-- [end:full_name_obj-java]
