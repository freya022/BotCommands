package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.internal.AbstractCommandInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.requireFirstParam
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.full.valueParameters

abstract class ApplicationCommandInfo protected constructor(
    context: BContext,
    builder: ApplicationCommandBuilder
) : AbstractCommandInfo<ApplicationCommand>(context, builder) {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
    val isTestOnly: Boolean

    init {
        scope = builder.scope
        isDefaultLocked = builder.defaultLocked
        isGuildOnly = context.applicationCommandsContext.isForceGuildCommandsEnabled || scope.isGuildOnly
        isTestOnly = builder.testOnly

        requireFirstParam(method.valueParameters, GlobalSlashEvent::class)

        if(isTestOnly && scope != CommandScope.GUILD) {
            throwUser("Application command annotated with @Test must have the GUILD scope")
        }
        if (isOwnerRequired) {
            throwUser("Application commands cannot be marked as owner-only")
        }

        //Administrators manage who can use what, bot doesn't need to check for user mistakes
        // Why would you ask for a permission if the administrators want a less-powerful user to be able to use it ?
        if (isDefaultLocked) {
            userPermissions.clear()
        }

        if (!isGuildOnly && (userPermissions.isNotEmpty() || botPermissions.isNotEmpty())) {
            throwUser("Application command with permissions should be guild-only")
        }

        if (commandId != null && scope != CommandScope.GUILD) {
            throwUser("Application command with guild-specific ID must have the GUILD scope")
        }
    }

    abstract override fun getParameters(): MethodParameters<out ApplicationCommandParameter<*>>

    @Suppress("UNCHECKED_CAST")
    override fun getOptionParameters(): List<ApplicationCommandParameter<*>> {
        return super.getOptionParameters() as List<ApplicationCommandParameter<*>>
    }
}