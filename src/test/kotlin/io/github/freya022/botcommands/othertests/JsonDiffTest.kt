package io.github.freya022.botcommands.othertests

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.api.core.utils.readResource
import io.github.freya022.botcommands.internal.commands.application.diff.DiffLogger
import io.github.freya022.botcommands.internal.commands.application.diff.DiffLoggerImpl
import io.github.freya022.botcommands.test.config.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object JsonDiffTest {
    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getLogger(DiffLogger::class.java) as Logger).level = Level.TRACE
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different command description`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_command_description", shouldBeEqual = false)
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different option order`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_option_order", shouldBeEqual = false)
    }

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Different command order`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "diff_command_order", shouldBeEqual = true)
    }

    @Suppress("UNCHECKED_CAST")
    private fun runTest(diffEngine: DiffEngine, folderName: String, shouldBeEqual: Boolean) {
        val oldMap = readResource("/commands_data/$folderName/old.json").let(DefaultObjectMapper::readList) as List<Map<String, *>>
        val newMap = readResource("/commands_data/$folderName/new.json").let(DefaultObjectMapper::readList) as List<Map<String, *>>

        DiffLoggerImpl("tests").apply {
            val isEqual = diffEngine.instance.checkCommands(oldMap, newMap)
            printLogs()
            assertEquals(shouldBeEqual, isEqual)
        }
    }
}