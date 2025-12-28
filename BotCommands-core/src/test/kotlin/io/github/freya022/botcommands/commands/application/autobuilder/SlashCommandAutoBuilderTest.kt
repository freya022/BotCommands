package io.github.freya022.botcommands.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandDeclarationFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager.Defaults
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashCommandGroupData
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.SlashCommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashSubcommandGroupBuilderImpl
import io.github.freya022.botcommands.internal.commands.autobuilder.checkDeclarationFilter
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.mockk.*
import net.dv8tion.jda.api.entities.Guild
import org.junit.jupiter.api.*
import kotlin.test.Test
import kotlin.test.assertContains

class SlashCommandAutoBuilderTest {
    private val serviceContainer = mockk<ServiceContainer>()
    private val applicationConfig = mockk<BApplicationConfig> {
        every { forceGuildCommands } returns false
    }
    private val resolverContainer = mockk<ResolverContainer>()
    private val functionAnnotationsMap = mockk<FunctionAnnotationsMap>()

    @AfterEach
    fun tearDown() {
        try {
            checkUnnecessaryStub()
        } finally {
            // "finally" avoids keeping unnecessary stub errors from one test to another
            clearAllMocks()
        }
    }

    @Nested
    inner class CommandsWithSamePath : ApplicationCommand() {
        @JDASlashCommand(name = "test_command")
        fun command1(event: GlobalSlashEvent) { consume(event) }

        @JDASlashCommand(name = "test_command")
        fun command2(event: GlobalSlashEvent) { consume(event) }

        @Test
        fun `Cannot have multiple commands with same path`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithSamePath, CommandsWithSamePath::command1),
                    ClassPathFunction(this@CommandsWithSamePath, CommandsWithSamePath::command2),
                )
            }

            val exception = assertThrows<IllegalStateException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Multiple annotated commands share the same path")
        }
    }

    @Nested
    inner class CommandAnnotations : ApplicationCommand() {
        @JDASlashCommand(name = "test_command", subcommand = "sub1")
        fun subcommand1(event: GlobalSlashEvent) { consume(event) }

        @JDASlashCommand(name = "test_command", subcommand = "sub2")
        fun subcommand2(event: GlobalSlashEvent) { consume(event) }

        @TopLevelSlashCommandData
        @JDASlashCommand(name = "test_command", subcommand = "sub3")
        fun subcommand3(event: GlobalSlashEvent) { consume(event) }

        @TopLevelSlashCommandData
        @SlashCommandGroupData
        @JDASlashCommand(name = "test_command", group = "group", subcommand = "sub1")
        fun groupSubcommand1(event: GlobalSlashEvent) { consume(event) }

        @SlashCommandGroupData
        @JDASlashCommand(name = "test_command", group = "group", subcommand = "sub2")
        fun groupSubcommand2(event: GlobalSlashEvent) { consume(event) }

        @Test
        fun `Check subcommands are missing top level annotation`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::subcommand1),
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::subcommand2),
                )
            }

            val exception = assertThrows<IllegalArgumentException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Subcommands must have at least one function be annotated with ${annotationRef<TopLevelSlashCommandData>()}")
        }

        @Test
        fun `Check subcommands have top level annotation`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::subcommand2),
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::subcommand3),
                )
            }

            assertDoesNotThrow {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }
        }

        @Test
        fun `Check group subcommands cannot have more than one group annotation`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::groupSubcommand1),
                    ClassPathFunction(this@CommandAnnotations, CommandAnnotations::groupSubcommand2),
                )
            }

            val exception = assertThrows<IllegalStateException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Cannot have multiple ${annotationRef<SlashCommandGroupData>()} on a same subcommand group")
        }
    }

    @Nested
    inner class CommandsWithMixedPathLengths : ApplicationCommand() {
        @JDASlashCommand(name = "test_command")
        fun topLevel(event: GlobalSlashEvent) { consume(event) }

        @TopLevelSlashCommandData
        @JDASlashCommand(name = "test_command", subcommand = "subcommand")
        fun subcommand(event: GlobalSlashEvent) { consume(event) }

        @JDASlashCommand(name = "test_command", group = "group")
        fun incompleteGroupSubcommand(event: GlobalSlashEvent) { consume(event) }

        @JDASlashCommand(name = "test_command", group = "group", subcommand = "subcommand")
        fun groupSubcommand(event: GlobalSlashEvent) { consume(event) }

        @Test
        fun `Cannot have command with group set but subcommands unset`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::incompleteGroupSubcommand),
                )
            }

            val exception = assertThrows<IllegalArgumentException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Slash commands with groups need to have their subcommand name set")
        }

        @Test
        fun `Cannot have top level and subcommand`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::topLevel),
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::subcommand),
                )
            }

            val exception = assertThrows<IllegalStateException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Cannot have both top level commands with subcommands")
        }

        @Test
        fun `Cannot have top level and group subcommand`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::topLevel),
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::groupSubcommand),
                )
            }

            val exception = assertThrows<IllegalStateException> {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }

            assertContains(exception.message!!, "Cannot have both top level commands with subcommands")
        }

        @Test
        fun `Allow subcommand with group subcommand`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::subcommand),
                    ClassPathFunction(this@CommandsWithMixedPathLengths, CommandsWithMixedPathLengths::groupSubcommand),
                )
            }

            assertDoesNotThrow {
                SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
            }
        }
    }

    @Nested
    inner class CommandsWithDeclarationFilters : ApplicationCommand() {
        // TODO test that DeclarationFilter only works on guild commands (throw otherwise)

        inner class DoNotDeclare : CommandDeclarationFilter {
            override fun filter(guild: Guild, path: CommandPath, commandId: String?): Boolean = false
        }

        @BeforeEach
        fun init() {
            every { serviceContainer.getService(DoNotDeclare::class) } returns DoNotDeclare()
        }

        @TopLevelSlashCommandData(scope = CommandScope.GUILD)
        @JDASlashCommand(name = "test_command")
        @DeclarationFilter(DoNotDeclare::class)
        fun topLevel(event: GlobalSlashEvent) { consume(event) }

        @TopLevelSlashCommandData(scope = CommandScope.GUILD)
        @JDASlashCommand(name = "test_command", subcommand = "subcommand")
        @DeclarationFilter(DoNotDeclare::class)
        fun subcommand(event: GlobalSlashEvent) { consume(event) }


        @DeclarationFilter(DoNotDeclare::class)
        @TopLevelSlashCommandData(scope = CommandScope.GUILD)
        @JDASlashCommand(name = "test_command", group = "group1", subcommand = "add")
        fun group1Subcommand(event: GlobalSlashEvent) { consume(event) }

        @JDASlashCommand(name = "test_command", group = "group2", subcommand = "get")
//        @DeclarationFilter(DoNotDeclare::class) // Do not filter out!
        fun group2Subcommand(event: GlobalSlashEvent) { consume(event) }

        @Test
        fun `Top-level command is filtered`() {
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithDeclarationFilters, CommandsWithDeclarationFilters::topLevel),
                )
            }

            mockkStatic("io.github.freya022.botcommands.internal.commands.autobuilder.AutoBuilderUtilsKt") {
                val autoBuilder = SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
                val manager = mockk<GuildApplicationCommandManager> {
                    every { guild } returns mockk()
                    every { context } returns mockk()
                }
                autoBuilder.declareGuildApplicationCommands(manager)

                verify(exactly = 1) { context(autoBuilder, any<SkipLogger>()) { checkDeclarationFilter(manager, any(), any(), any()) } }
                verify(exactly = 0) { manager.slashCommand(any(), any(), any()) }
            }
        }

        @Test
        fun `All subcommands being filtered also disables the top-level command`() {
            // 1 subcommand, filter it, check top level isn't declared
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithDeclarationFilters, CommandsWithDeclarationFilters::subcommand),
                )
            }

            mockkStatic("io.github.freya022.botcommands.internal.commands.autobuilder.AutoBuilderUtilsKt") {
                val autoBuilder = SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)
                val manager = mockk<GuildApplicationCommandManager> {
                    every { guild } returns mockk()
                    every { context } returns mockk()
                }
                autoBuilder.declareGuildApplicationCommands(manager)

                verify(exactly = 0) { manager.slashCommand(any(), any(), any()) }
                verify(exactly = 1) { context(autoBuilder, any<SkipLogger>()) { checkDeclarationFilter(manager, any(), any(), any()) } }
            }
        }

        @Test
        fun `All group subcommands being filtered also disables the group and top-level command`() {
            // 1 group, 1 subcommand, filter subcommand, check nothing is declared
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithDeclarationFilters, CommandsWithDeclarationFilters::group1Subcommand),
                )
            }

            val autoBuilder = SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)

            val manager = mockk<GuildApplicationCommandManager> {
                every { guild } returns mockk()
                every { context } returns mockk()
            }

            mockkStatic("io.github.freya022.botcommands.internal.commands.autobuilder.AutoBuilderUtilsKt") {
                autoBuilder.declareGuildApplicationCommands(manager)

                verify(exactly = 0) { manager.slashCommand(any(), any(), any()) }
                verify(exactly = 1) { context(autoBuilder, any<SkipLogger>()) { checkDeclarationFilter(manager, any(), any(), any()) } }
            }
        }

        @Test
        fun `A subcommand being filtered off a single-child group also disables the group`() {
            // 2 groups, 1 subcommand each, only one of them gets filtered out, check group is removed and other is kept
            every { functionAnnotationsMap.getWithClassAnnotation(Command::class, JDASlashCommand::class) } answers {
                listOf(
                    ClassPathFunction(this@CommandsWithDeclarationFilters, CommandsWithDeclarationFilters::group1Subcommand),
                    ClassPathFunction(this@CommandsWithDeclarationFilters, CommandsWithDeclarationFilters::group2Subcommand),
                )
            }

            val autoBuilder = SlashCommandAutoBuilder(serviceContainer, applicationConfig, resolverContainer, functionAnnotationsMap)

            // Mock builders and run the real lambdas against them,
            // so we can record the calls they do
            val groupBuilder = mockk<SlashSubcommandGroupBuilderImpl>(relaxed = true)
            val builder = mockk<TopLevelSlashCommandBuilder>(relaxed = true) {
                every { subcommandGroup(any(), any()) } answers {
                    groupBuilder.apply(lastArg())
                    return@answers
                }
            }

            val manager = mockk<GuildApplicationCommandManager>{
                every { slashCommand(any(), any(), any()) } answers {
                    builder.apply(lastArg())
                    return@answers
                }
                every { guild } returns mockk()
                every { context } returns mockk()
                every { defaultContexts } returns Defaults.contexts
                every { defaultIntegrationTypes } returns Defaults.integrationTypes
            }

            autoBuilder.declareGuildApplicationCommands(manager)

            verify(exactly = 0) { builder.subcommand(any(), any(), any()) }
            verify(exactly = 1) { builder.subcommandGroup("group2", any()) }
            verify(exactly = 1) { groupBuilder.subcommand(any(), any(), any()) }
        }
    }

    // TODO test that Test only works on guild commands (throw otherwise)

    @Suppress("NOTHING_TO_INLINE", "unused")
    private inline fun consume(e: Any) {}
}
