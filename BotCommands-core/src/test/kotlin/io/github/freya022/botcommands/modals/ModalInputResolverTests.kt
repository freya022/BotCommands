package io.github.freya022.botcommands.modals

import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.entities.asInputUser
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.modals.resolvers.*
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.Component.Type.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.full.valueParameters
import kotlin.test.assertEquals

object ModalInputResolverTests {

    private const val STRING = "Sample text"
    private val strings = listOf(STRING)
    private val role = mockk<Role>()
    private val roles = listOf(role)
    private val user = mockk<User> {
        every { idLong } returns 1234
    }
    private val users = listOf(user)
    private val member = mockk<Member> {
        every { idLong } returns 1234
        every { user } returns this@ModalInputResolverTests.user
    }
    private val inputUser = member.asInputUser()
    private val inputUsers = listOf(inputUser)
    private val members = listOf(member)
    private val channel = mockk<GuildChannel>()
    private val channels = listOf(channel)
    private val mentions = mockk<Mentions> {
        every { users } returns this@ModalInputResolverTests.users
        every { members } returns this@ModalInputResolverTests.members
        every { roles } returns this@ModalInputResolverTests.roles
        every { channels } returns this@ModalInputResolverTests.channels
        every { getMentions() } returns this@ModalInputResolverTests.roles
    }
    private val emptyMentions = mockk<Mentions> {
        every { users } returns emptyList()
        every { members } returns emptyList()
        every { roles } returns emptyList()
        every { channels } returns emptyList()
        every { getMentions() } returns emptyList()
    }
    private val attachments = listOf<Message.Attachment>(mockk {
        every { close() } just runs
    })

    @MethodSource("modalInputs")
    @ParameterizedTest
    suspend fun <R> `Modal input parameter can be resolved`(parameterName: String, type: Component.Type, getter: (ModalMapping) -> R, value: R, expected: Any?) {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf(
                "modalMentionsResolver",
                "modalStringResolver",
                "modalStringListResolver",
                "modalAttachmentResolver",
                "modalAttachmentListResolver",
                "modalBooleanResolver",
            )

            every { findAnnotationOnService("modalMentionsResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringListResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalAttachmentResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalAttachmentListResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalBooleanResolver", Resolver::class) } returns Resolver(0)

            every { getService("modalMentionsResolver", ParameterResolver::class) } returns ModalMentionsResolver
            every { getService("modalStringResolver", ParameterResolver::class) } returns ModalStringResolver
            every { getService("modalStringListResolver", ParameterResolver::class) } returns ModalStringListResolver
            every { getService("modalAttachmentResolver", ParameterResolver::class) } returns ModalAttachmentResolver
            every { getService("modalAttachmentListResolver", ParameterResolver::class) } returns ModalAttachmentListResolver
            every { getService("modalBooleanResolver", ParameterResolver::class) } returns ModalBooleanResolver
        }
        val resolvers = ResolverContainer(serviceContainer, listOf(ModalIMentionableResolverFactory))

        val parameter = ::userFunc.valueParameters.single { it.name == parameterName }
        val request = ResolverRequest(ParameterWrapper(parameter))

        val resolver = resolvers.getResolver(ModalParameterResolver::class, request)
        val modalMapping = mockk<ModalMapping> {
            every { getter(this@mockk) } returns value
            every { this@mockk.type } returns type
        }

        val value = resolver.resolveSuspend(
            mockk<ModalOption> {
               every { isRequired } returns !(parameter.isOptional || parameter.type.isMarkedNullable)
            },
            mockk<ModalEvent>(),
            modalMapping,
        )

        assertEquals(expected, value)
    }

    @JvmStatic
    fun modalInputs(): List<Arguments> {
        val listOf = listOf(
            arguments(
                type = TEXT_INPUT,
                name = "<string> -> <string>",
                getter = ModalMapping::getAsString to STRING,
                parameterName = "string",
                expectedParameterValue = STRING
            ),
            arguments(
                type = TEXT_INPUT,
                name = "<string> -> <string> (nullable)",
                getter = ModalMapping::getAsOptionalString to STRING,
                parameterName = "nullableString",
                expectedParameterValue = STRING,
            ),
            arguments(
                type = TEXT_INPUT,
                name = "<empty> -> null",
                getter = ModalMapping::getAsOptionalString to null,
                parameterName = "nullableString",
                expectedParameterValue = null
            ),
            arguments(
                type = TEXT_INPUT,
                name = "<empty> -> <empty>",
                getter = ModalMapping::getAsString to "",
                parameterName = "string",
                expectedParameterValue = ""
            ),

            arguments(
                type = STRING_SELECT,
                name = "<list with single value> -> <value>",
                getter = ModalMapping::getAsStringList to strings,
                parameterName = "string",
                expectedParameterValue = STRING
            ),
            arguments(
                type = STRING_SELECT,
                name = "<list> -> <list>",
                getter = ModalMapping::getAsStringList to strings,
                parameterName = "stringList",
                expectedParameterValue = strings
            ),
            arguments(
                type = STRING_SELECT,
                name = "<empty list> -> null",
                getter = ModalMapping::getAsStringList to emptyList(),
                parameterName = "nullableString",
                expectedParameterValue = null
            ),

            arguments(
                type = MENTIONABLE_SELECT,
                name = "<mentions with single value> -> <mentionable>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "mentionable",
                expectedParameterValue = role
            ),
            arguments(
                type = MENTIONABLE_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "mentionableList",
                expectedParameterValue = roles
            ),
            arguments(
                type = MENTIONABLE_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "mentionable",
                expectedParameterValue = null
            ),

            arguments(
                type = ROLE_SELECT,
                name = "<mentions with single value> -> <role>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "role",
                expectedParameterValue = role
            ),
            arguments(
                type = ROLE_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "roleList",
                expectedParameterValue = roles
            ),
            arguments(
                type = ROLE_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "role",
                expectedParameterValue = null
            ),

            arguments(
                type = USER_SELECT,
                name = "<mentions with single value> -> <user>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "user",
                expectedParameterValue = user
            ),
            arguments(
                type = USER_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "userList",
                expectedParameterValue = users
            ),
            arguments(
                type = USER_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "user",
                expectedParameterValue = null
            ),

            arguments(
                type = USER_SELECT,
                name = "<mentions with single value> -> <input user>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "inputUser",
                expectedParameterValue = inputUser
            ),
            arguments(
                type = USER_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "inputUserList",
                expectedParameterValue = inputUsers
            ),
            arguments(
                type = USER_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "inputUser",
                expectedParameterValue = null
            ),

            arguments(
                type = USER_SELECT,
                name = "<mentions with single value> -> <member>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "member",
                expectedParameterValue = member
            ),
            arguments(
                type = USER_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "memberList",
                expectedParameterValue = members
            ),
            arguments(
                type = USER_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "member",
                expectedParameterValue = null
            ),

            arguments(
                type = CHANNEL_SELECT,
                name = "<mentions with single value> -> <channel>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "channel",
                expectedParameterValue = channel
            ),
            arguments(
                type = CHANNEL_SELECT,
                name = "<mentions> -> <list>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "channelList",
                expectedParameterValue = channels
            ),
            arguments(
                type = CHANNEL_SELECT,
                name = "<empty mentions> -> null",
                getter = ModalMapping::getAsMentions to emptyMentions,
                parameterName = "channel",
                expectedParameterValue = null
            ),

            arguments(
                type = MENTIONABLE_SELECT,
                name = "<mentions> -> <mentions>",
                getter = ModalMapping::getAsMentions to mentions,
                parameterName = "mentions",
                expectedParameterValue = mentions
            ),

            arguments(
                type = FILE_UPLOAD,
                name = "<list with single value> -> <value>",
                getter = ModalMapping::getAsAttachmentList to attachments,
                parameterName = "attachment",
                expectedParameterValue = attachments.first()
            ),
            arguments(
                type = FILE_UPLOAD,
                name = "<list> -> <list>",
                getter = ModalMapping::getAsAttachmentList to attachments,
                parameterName = "attachmentList",
                expectedParameterValue = attachments
            ),
            arguments(
                type = FILE_UPLOAD,
                name = "<empty list> -> null",
                getter = ModalMapping::getAsAttachmentList to emptyList(),
                parameterName = "attachment",
                expectedParameterValue = null
            ),

            arguments(
                type = CHECKBOX,
                name = "<boolean> -> <boolean>",
                getter = ModalMapping::getAsBoolean to true,
                parameterName = "boolean",
                expectedParameterValue = true
            ),

            arguments(
                type = CHECKBOX_GROUP,
                name = "<list with single value> -> <value>",
                getter = ModalMapping::getAsStringList to strings,
                parameterName = "string",
                expectedParameterValue = STRING
            ),
            arguments(
                type = CHECKBOX_GROUP,
                name = "<list> -> <list>",
                getter = ModalMapping::getAsStringList to strings,
                parameterName = "stringList",
                expectedParameterValue = strings
            ),
            arguments(
                type = CHECKBOX_GROUP,
                name = "<empty list> -> null",
                getter = ModalMapping::getAsStringList to emptyList(),
                parameterName = "nullableString",
                expectedParameterValue = null
            ),

            arguments(
                type = RADIO_GROUP,
                name = "<string> -> <string>",
                getter = ModalMapping::getAsOptionalString to STRING,
                parameterName = "string",
                expectedParameterValue = STRING
            ),
            arguments(
                type = RADIO_GROUP,
                name = "<no selection> -> null",
                getter = ModalMapping::getAsOptionalString to null,
                parameterName = "nullableString",
                expectedParameterValue = null
            ),
        )
        return listOf
    }

    private fun <R> arguments(type: Component.Type, name: String, getter: Pair<(ModalMapping) -> R, R>, parameterName: String, expectedParameterValue: Any?) =
        argumentSet("[${type.name}] $name", parameterName, type, getter.first, getter.second, expectedParameterValue)

    private fun userFunc(
        @Suppress("unused") string: String,
        @Suppress("unused") nullableString: String?,
        @Suppress("unused") stringList: List<String>,
        @Suppress("unused") mentionable: IMentionable?,
        @Suppress("unused") mentionableList: List<IMentionable>,
        @Suppress("unused") role: Role?,
        @Suppress("unused") roleList: List<Role>,
        @Suppress("unused") user: User?,
        @Suppress("unused") userList: List<User>,
        @Suppress("unused") inputUser: InputUser?,
        @Suppress("unused") inputUserList: List<InputUser>,
        @Suppress("unused") member: Member?,
        @Suppress("unused") memberList: List<Member>,
        @Suppress("unused") channel: GuildChannel?,
        @Suppress("unused") channelList: List<GuildChannel>,
        @Suppress("unused") mentions: Mentions,
        @Suppress("unused") attachment: Message.Attachment?,
        @Suppress("unused") attachmentList: List<Message.Attachment>,
        @Suppress("unused") boolean: Boolean,
    ) {}
}
