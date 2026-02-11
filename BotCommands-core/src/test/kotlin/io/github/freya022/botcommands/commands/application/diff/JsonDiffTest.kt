package io.github.freya022.botcommands.commands.application.diff

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.freya022.botcommands.api.core.utils.readResource
import io.github.freya022.botcommands.internal.commands.application.diff.DiffLoggerImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

object JsonDiffTest {

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

    @ParameterizedTest
    @EnumSource(DiffEngine::class)
    fun `Add option in subcommand`(diffEngine: DiffEngine) {
        runTest(diffEngine, folderName = "add_option_in_subcommand", shouldBeEqual = false)
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
