package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference
import net.dv8tion.jda.api.Permission
import java.util.EnumSet

/**
 * **Internal** interface representing a command "unit".
 * A unit may be different in other contexts, for example,
 * a slash command unit is a single function,
 * while a text command unit is a set of functions, called "variations".
 *
 * A unit is basically a set of functions sharing the same path.
 */
interface CommandUnit {

    /**
     * When there are multiple functions, this returns one of them.
     */
    fun getBestDeclarationSite(): DeclarationSite

    context(builder: CommandBuilder)
    fun getRateLimiter(): RateLimiter?

    context(builder: CommandBuilder)
    fun getRateLimitRef(): RateLimitReference?

    context(builder: CommandBuilder)
    fun getBotPermissions(): EnumSet<Permission>

    context(builder: CommandBuilder)
    fun getUserPermissions(): EnumSet<Permission>
}
