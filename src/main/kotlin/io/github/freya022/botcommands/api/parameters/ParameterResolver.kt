package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.annotations.MentionsString
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.parameters.resolvers.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji

// NOTE: you can edit the built-in resolvers docs using https://www.tablesgenerator.com/markdown_tables
// see the File > Paste table data
/**
 * Base class for parameter resolvers used in text commands, application commands, and component callbacks.
 *
 * You need to extend [ClassParameterResolver] or [TypedParameterResolver] instead.
 *
 * ### Default parameter resolvers
 *
 * |                                                                 | Text              | Slash           | Message context    | User context    | Components | Modals |
 * |-----------------------------------------------------------------|-------------------|-----------------|--------------------|-----------------|------------|--------|
 * | [String]                                                        | ✓ (can be quoted) | ✓               |                    |                 | ✓          | ✓      |
 * | [Boolean]                                                       | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Int]                                                           | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Long]                                                          | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Double]                                                        | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Emoji]                                                         | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [IMentionable]                                                  | ✓ (only mentions) | ✓               |                    |                 |            |        |
 * | [List] of mentionable (see [@MentionsString][MentionsString])   |                   | ✓               |                    |                 |            |        |
 * | [Role]                                                          | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [User]                                                          | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [Member]                                                        | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [InputUser]                                                     | ✓                 | ✓               |                    | ✓ (target user) | ✓          |        |
 * | [All guild channels subtypes (includes Category)][GuildChannel] | ✓                 | ✓               |                    |                 | ✓          |        |
 * | [Guild]                                                         | ✓                 | ✓ (as a String) |                    |                 | ✓          |        |
 * | [Message]                                                       |                   |                 | ✓ (target message) |                 |            |        |
 * | [Attachment]                                                    |                   | ✓               |                    |                 |            |        |
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
 * @see ICustomResolver
 */
@InterfacedService(acceptMultiple = true)
sealed class ParameterResolver<T : ParameterResolver<T, R>, R : Any>
