package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes
import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.parameters.resolvers.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * Base class for parameter resolvers used in text commands, application commands, and component callbacks.
 *
 * You need to extend [ClassParameterResolver] or [TypedParameterResolver] instead.
 *
 * ### Default parameter resolvers
 *
 * |                                                               | Text              | Slash           | Message context    | User context    | Components | Modals | Component timeout |
 * |---------------------------------------------------------------|-------------------|-----------------|--------------------|-----------------|------------|--------|-------------------|
 * | [String]                                                      | ✓ (can be quoted) | ✓               |                    |                 | ✓          | ✓      | ✓
 * | [Boolean]                                                     | ✓                 | ✓               |                    |                 | ✓          |        | ✓
 * | [Int]                                                         | ✓                 | ✓               |                    |                 | ✓          |        | ✓
 * | [Long]                                                        | ✓                 | ✓               |                    |                 | ✓          |        | ✓
 * | [Double]                                                      | ✓                 | ✓               |                    |                 | ✓          |        | ✓
 * | [Emoji]                                                       | ✓                 | ✓               |                    |                 | ✓          |        | ✓
 * | [IMentionable]                                                | ✓ (only mentions) | ✓               |                    |                 |            |        |
 * | [List] of mentionable (see [@MentionsString][MentionsString]) |                   | ✓               |                    |                 |            |        |
 * | [Role]                                                        | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [User]                                                        | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [Member]                                                      | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [InputUser]                                                   | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [GuildChannel] subtypes<sup>1</sup>                           | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Guild]                                                       | ✓                 | ✓ (as a String) |                    |                 | ✓          |        |
 * | [Message]                                                     |                   |                 | ✓ (target message) |                 |            |        |
 * | [Attachment]                                                  |                   | ✓               |                    |                 |            |        |
 *
 * 1. The channel types are set automatically depending on the type,
 * but a broader channel type can be used
 * and restricted to multiple concrete types by using [@ChannelTypes][ChannelTypes].
 *
 * Parameter resolvers for services exist by default, and follow the rules described in [@BService][BService].
 *
 * You can also check loaded parameter resolvers in the logs on the `trace` level.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 *
 * @see ClassParameterResolver
 *
 * @see ParameterResolverFactory
 *
 * @see TextParameterResolver
 * @see QuotableTextParameterResolver
 * @see ComponentParameterResolver
 * @see SlashParameterResolver
 * @see MessageContextParameterResolver
 * @see UserContextParameterResolver
 * @see TimeoutParameterResolver
 * @see ICustomResolver
 *
 * @see Resolvers
 */
@InterfacedService(acceptMultiple = true)
sealed class ParameterResolver<T : ParameterResolver<T, R>, R : Any> : IParameterResolver<T>
