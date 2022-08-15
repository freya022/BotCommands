package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.AbstractCommandInfo
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.throwUser

abstract class ApplicationCommandInfo internal constructor(
    context: BContextImpl,
    builder: ApplicationCommandBuilder
) : AbstractCommandInfo(context, builder) {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
    val isTestOnly: Boolean

    override val instance: ApplicationCommand
        get() = super.instance as ApplicationCommand

    abstract override val parameters: MethodParameters
    override val optionParameters: List<MethodParameter>
        get() = super.optionParameters

    init {
        scope = builder.scope
        isDefaultLocked = builder.defaultLocked
        isGuildOnly = context.config.applicationConfig.forceGuildCommands || scope.isGuildOnly
        isTestOnly = builder.testOnly

        if (builder.cooldownStrategy.scope != CooldownScope.USER && !scope.isGuildOnly) {
            throwUser("Application command cannot have a ${builder.cooldownStrategy.scope} cooldown scope with a global slash command")
        }

        if(isTestOnly && scope != CommandScope.GUILD) {
            throwUser("Application command annotated with @Test must have the GUILD scope")
        }

        //Administrators manage who can use what, bot doesn't need to check for user mistakes
        // Why would you ask for a permission if the administrators want a less-powerful user to be able to use it ?
        if (isDefaultLocked) {
            userPermissions.clear()
        }

        if (!isGuildOnly && (userPermissions.isNotEmpty() || botPermissions.isNotEmpty())) {
            throwUser("Application command with permissions should be guild-only")
        }
    }
}