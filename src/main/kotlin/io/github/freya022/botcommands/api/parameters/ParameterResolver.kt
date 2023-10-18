package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * Base class for parameter resolvers used in text commands, application commands, and component callbacks.
 *
 * You need to extend [ClassParameterResolver] or [TypedParameterResolver] instead.
 *
 * ### Default parameter resolvers
 * - [String]
 *
 * - [Boolean]
 * - [Long]
 * - [Double]
 *
 * - [Emoji]
 *
 * - [Role]
 * - [User]
 * - [Member]
 * - [InputUser]
 * - [All guild channels subtypes][GuildChannel]
 * - [Message] (only message context commands)
 *
 * You can also check loaded parameter resolvers in the logs on the `trace` level.
 *
 * @see ClassParameterResolver
 *
 * @see ParameterResolverFactory
 *
 * @see RegexParameterResolver
 * @see QuotableRegexParameterResolver
 * @see ComponentParameterResolver
 * @see SlashParameterResolver
 * @see MessageContextParameterResolver
 * @see UserContextParameterResolver
 * @see ICustomResolver
 */
@InterfacedService(acceptMultiple = true)
sealed class ParameterResolver<T : ParameterResolver<T, R>, R : Any>
