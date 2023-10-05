package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.SimpleCommandMap
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import kotlin.reflect.KFunction

sealed class AbstractApplicationCommandManager(private val context: BContext) {
    private val commandMap: SimpleCommandMap<ApplicationCommandInfo> = SimpleCommandMap.ofInfos()
    internal val applicationCommands: Map<String, ApplicationCommandInfo>
        @JvmSynthetic get() = commandMap.map

    @JvmSynthetic
    internal abstract fun isValidScope(scope: CommandScope): Boolean

    protected abstract fun checkScope(scope: CommandScope)

    /**
     * Declares the supplied function as a slash command.
     *
     * Discord requires you to have:
     *  - 1 unique command name, examples:
     *    - `/nick`
     *
     *  - Multiple commands with the same base name but different subcommand names, examples:
     *    - `/info user`
     *    - `/info role`
     *    - `/info channel`
     *
     *  - Multiple subcommands with the same base name and base group but with different subcommand names, examples:
     *    - `/info simple user`
     *    - `/info simple role`
     *    - `/info complete user`
     *    - `/info complete role`
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups) for more details.
     *
     * **Requirement:** The declaring class must be annotated with [@Command][Command].
     *
     * @see Command @Command
     *
     * @see JDASlashCommand @JDASlashCommand
     */
    @JvmOverloads
    fun slashCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>?, builder: TopLevelSlashCommandBuilder.() -> Unit) {
        checkScope(scope)

        TopLevelSlashCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    /**
     * Declares the supplied function as a user context command.
     *
     * The targeted function must have a [GlobalUserEvent] or a [GuildUserEvent],
     * with the only accepted [options][ContextOption] being [Member], [User] and [InputUser],
     * which will be the *targeted* entity.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#user-commands) for more details.
     *
     * **Requirement:** The declaring class must be annotated with [@Command][Command].
     *
     * @see GlobalUserEvent.getTarget
     * @see GlobalUserEvent.getTargetMember
     *
     * @see Command @Command
     *
     * @see JDAUserCommand @JDAUserCommand
     */
    @JvmOverloads
    fun userCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: UserCommandBuilder.() -> Unit) {
        checkScope(scope)

        UserCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    /**
     * Declares the supplied function as a message context command.
     *
     * The targeted function must have a [GlobalMessageEvent] or a [GuildMessageEvent],
     * with the only accepted [option][MessageCommandBuilder.option] being [Message],
     * which will be the *targeted* message.
     *
     * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands#message-commands) for more details.
     *
     * **Requirement:** The declaring class must be annotated with [@Command][Command].
     *
     * @see GlobalMessageEvent.getTarget
     *
     * @see Command @Command
     *
     * @see JDAMessageCommand
     */
    @JvmOverloads
    fun messageCommand(name: String, scope: CommandScope = CommandScope.GLOBAL_NO_DM, function: KFunction<Any>, builder: MessageCommandBuilder.() -> Unit) {
        checkScope(scope)

        MessageCommandBuilder(context, name, function, scope)
            .apply(builder)
            .build()
            .also(::putNewCommand)
    }

    private fun putNewCommand(newInfo: ApplicationCommandInfo) {
        commandMap.putNewCommand(newInfo)
    }
}