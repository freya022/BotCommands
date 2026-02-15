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
    private val attachments = listOf<Message.Attachment>(mockk {
        every { close() } just runs
    })

    @MethodSource("modalInputs")
    @ParameterizedTest
    suspend fun `Modal input parameter can be resolved`(index: Int, type: Component.Type, expected: Any?) {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf(
                "modalMentionsResolver",
                "modalStringResolver",
                "modalStringListResolver",
                "modalAttachmentResolver",
                "modalAttachmentListResolver",
            )

            every { findAnnotationOnService("modalMentionsResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringListResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalAttachmentResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalAttachmentListResolver", Resolver::class) } returns Resolver(0)

            every { getService("modalMentionsResolver", ParameterResolver::class) } returns ModalMentionsResolver
            every { getService("modalStringResolver", ParameterResolver::class) } returns ModalStringResolver
            every { getService("modalStringListResolver", ParameterResolver::class) } returns ModalStringListResolver
            every { getService("modalAttachmentResolver", ParameterResolver::class) } returns ModalAttachmentResolver
            every { getService("modalAttachmentListResolver", ParameterResolver::class) } returns ModalAttachmentListResolver
        }
        val resolvers = ResolverContainer(serviceContainer, listOf(ModalIMentionableResolverFactory))

        val parameter = ::userFunc.valueParameters[index]
        val request = ResolverRequest(ParameterWrapper(parameter))

        val resolver = resolvers.getResolver(ModalParameterResolver::class, request)
        val modalMapping = mockk<ModalMapping> {
            every { asString } returns STRING
            every { asOptionalString } returns null
            every { asStringList } returns strings
            every { asMentions } returns mentions
            every { asAttachmentList } returns attachments
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
            arguments("TextInput String", 0, TEXT_INPUT, STRING),
            arguments("TextInput empty as null", 18, TEXT_INPUT, null),
            arguments("TextInput empty with default value", 19, TEXT_INPUT, null),
            arguments("Select menu string", 16, STRING_SELECT, STRING),
            arguments("Select menu strings", 1, STRING_SELECT, strings),
            arguments("Select menu mentionable", 2, MENTIONABLE_SELECT, role),
            arguments("Select menu mentionables", 3, MENTIONABLE_SELECT, roles),
            arguments("Select menu role", 4, ROLE_SELECT, role),
            arguments("Select menu roles", 5, ROLE_SELECT, roles),
            arguments("Select menu user", 6, USER_SELECT, user),
            arguments("Select menu users", 7, USER_SELECT, users),
            arguments("Select menu input user", 8, USER_SELECT, inputUser),
            arguments("Select menu input users", 9, USER_SELECT, inputUsers),
            arguments("Select menu member", 10, USER_SELECT, member),
            arguments("Select menu members", 11, USER_SELECT, members),
            arguments("Select menu channel", 12, CHANNEL_SELECT, channel),
            arguments("Select menu channels", 13, CHANNEL_SELECT, channels),
            arguments("Select menu mentions", 14, MENTIONABLE_SELECT, mentions),
            arguments("Attachment", 17, FILE_UPLOAD, attachments.first()),
            arguments("Attachments", 15, FILE_UPLOAD, attachments),
        )
        return listOf
    }

    private fun arguments(name: String, index: Int, type: Component.Type, expected: Any?) =
        argumentSet(name, index, type, expected)

    private fun userFunc(
        @Suppress("unused") textInput: String,
        @Suppress("unused") selectedStrings: List<String>,
        @Suppress("unused") selectedMentionable: IMentionable?,
        @Suppress("unused") selectedMentionables: List<IMentionable>,
        @Suppress("unused") selectedRole: Role?,
        @Suppress("unused") selectedRoles: List<Role>,
        @Suppress("unused") selectedUser: User?,
        @Suppress("unused") selectedUsers: List<User>,
        @Suppress("unused") selectedInputUser: InputUser?,
        @Suppress("unused") selectedInputUsers: List<InputUser>,
        @Suppress("unused") selectedMember: Member?,
        @Suppress("unused") selectedMembers: List<Member>,
        @Suppress("unused") selectedChannel: GuildChannel?,
        @Suppress("unused") selectedChannels: List<GuildChannel>,
        @Suppress("unused") selectedMentions: Mentions,
        @Suppress("unused") attachments: List<Message.Attachment>,
        @Suppress("unused") selectedString: String,
        @Suppress("unused") attachment: Message.Attachment,
        @Suppress("unused") emptyTextInputAsNull: String?,
        @Suppress("unused") emptyTextInputAsOptional: String = "default value",
    ) {}
}
