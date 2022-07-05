package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionCacheMode
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode
import com.freya02.botcommands.api.builder.BuilderFunctionHolder
import com.freya02.botcommands.internal.throwUser

class AutocompleteInfoBuilder internal constructor() : BuilderFunctionHolder<Collection<*>>() {
    var mode: AutocompletionMode = AutocompletionMode.FUZZY //TODO impl
    var showUserInput: Boolean = false //TODO impl
    var cacheMode: AutocompletionCacheMode = AutocompletionCacheMode.NO_CACHE //TODO impl
    var cacheSize: Long = 2048 //TODO impl
    var guildLocal: Boolean = false //TODO impl
    var userLocal: Boolean = false //TODO impl
    var channelLocal: Boolean = false //TODO impl

    internal fun build(): AutocompleteInfo {
        if (!isFunctionInitialized()) {
            throwUser("An autocompleted option must have its function set")
        }

        return AutocompleteInfo(this)
    }
}