package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.annotations.InjectedService

@InjectedService
class BDebugConfig internal constructor() {
    /**
     * Whether the differences between old and new application commands data should be logged
     */
    var enableApplicationDiffsLogs: Boolean = false

    /**
     * Whether the missing localization strings when creation the command objects should be logged
     */
    var enabledMissingLocalizationLogs: Boolean = false
}
