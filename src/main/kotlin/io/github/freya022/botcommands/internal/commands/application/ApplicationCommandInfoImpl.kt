package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.jvm.jvmErasure

internal abstract class ApplicationCommandInfoImpl internal constructor(
    builder: ApplicationCommandBuilder<*>
) : AbstractCommandInfoImpl(builder),
    ApplicationCommandInfo,
    ExecutableMixin {

    internal val filters: List<ApplicationCommandFilter<*>> = builder.filters.onEach { filter ->
        require(!filter.global) {
            "Global filter ${filter.javaClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
        }
    }

    override fun hasFilters(): Boolean = filters.isNotEmpty()

    // Using the builder to get the scope is required as the info object is still initializing
    // and would NPE when getting the top level instance
    protected inline fun <reified GUILD_T : GenericCommandInteractionEvent> MemberParamFunction<out GenericCommandInteractionEvent, *>.checkEventScope(
        builder: ApplicationCommandBuilder<*>
    ) {
        if (kFunction.isFakeSlashFunction()) return

        val eventType = firstParameter.type.jvmErasure
        if (builder.topLevelBuilder.scope.isGuildOnly) {
            if (!eventType.isSubclassOf<GUILD_T>()) {
                // Do not warn about guild-restricted types when everything is forced as a guild command
                if (builder.context.applicationConfig.forceGuildCommands) return

                Logging.getLogger().warn("${kFunction.shortSignature} : First parameter could be a ${classRef<GUILD_T>()} as to benefit from non-null getters")
            }
        } else if (eventType.isSubclassOf<GUILD_T>()) {
            throwArgument(kFunction, "Cannot use ${classRef<GUILD_T>()} on a global application command")
        }
    }
}