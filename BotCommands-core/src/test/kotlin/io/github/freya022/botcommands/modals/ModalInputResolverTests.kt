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
import io.mockk.mockk
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
    private val attachments = listOf<Message.Attachment>(mockk())

    @MethodSource("modalInputs")
    @ParameterizedTest
    suspend fun `Modal input parameter can be resolved`(index: Int, expected: Any?) {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf(
                "modalMentionsResolver",
                "modalStringResolver",
                "modalStringListResolver",
                "modalAttachmentListResolver",
            )

            every { findAnnotationOnService("modalMentionsResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalStringListResolver", Resolver::class) } returns Resolver(0)
            every { findAnnotationOnService("modalAttachmentListResolver", Resolver::class) } returns Resolver(0)

            every { getService("modalMentionsResolver", ParameterResolver::class) } returns ModalMentionsResolver
            every { getService("modalStringResolver", ParameterResolver::class) } returns ModalStringResolver
            every { getService("modalStringListResolver", ParameterResolver::class) } returns ModalStringListResolver
            every { getService("modalAttachmentListResolver", ParameterResolver::class) } returns ModalAttachmentListResolver
        }
        val resolvers = ResolverContainer(serviceContainer, listOf(ModalIMentionableResolverFactory))

        val request = ResolverRequest(ParameterWrapper(::userFunc.valueParameters[index]))

        val resolver = resolvers.getResolver(ModalParameterResolver::class, request)
        val modalMapping = mockk<ModalMapping> {
            every { asString } returns STRING
            every { asStringList } returns strings
            every { asMentions } returns mentions
            every { asAttachmentList } returns attachments
        }

        val value = resolver.resolveSuspend(
            mockk<ModalOption>(),
            mockk<ModalEvent>(),
            modalMapping,
        )

        assertEquals(expected, value)
    }

    @JvmStatic
    fun modalInputs(): List<Arguments> {
        val listOf = listOf(
            arguments("TextInput String", 0, STRING),
            arguments("Select menu strings", 1, strings),
            arguments("Select menu mentionable", 2, role),
            arguments("Select menu mentionables", 3, roles),
            arguments("Select menu role", 4, role),
            arguments("Select menu roles", 5, roles),
            arguments("Select menu user", 6, user),
            arguments("Select menu users", 7, users),
            arguments("Select menu input user", 8, inputUser),
            arguments("Select menu input users", 9, inputUsers),
            arguments("Select menu member", 10, member),
            arguments("Select menu members", 11, members),
            arguments("Select menu channel", 12, channel),
            arguments("Select menu channels", 13, channels),
            arguments("Select menu mentions", 14, mentions),
            arguments("Attachments", 15, attachments),
        )
        return listOf
    }

    private fun arguments(name: String, index: Int, expected: Any?) =
        argumentSet(name, index, expected)

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
    ) {}
}
