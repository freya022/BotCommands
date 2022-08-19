package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.prefixed.TextCommandsContext
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser

class TextCommandsContextImpl internal constructor(context: BContextImpl) : TextCommandsContext {
    private val treeRoot = TextCommandTree()

    fun addTextCommand(commandInfo: TextCommandInfo) {
        val tree = commandInfo.path.fullPath
            .split('/')
            .dropLast(1)
            .fold(treeRoot) { tree, it ->
                tree.children.computeIfAbsent(it) { TextCommandTree() }
            }

        (commandInfo.aliases + commandInfo.path).forEach { path ->
            val currentTree = tree.children.computeIfAbsent(path.lastName) { TextCommandTree() }

            if (currentTree.exists(commandInfo)) throwUser(commandInfo.method, "Text command with path ${commandInfo.path} already exists")

            if (!currentTree.addCommand(commandInfo)) throwUser(commandInfo.method, "Text command with path ${commandInfo.path} already exists")
        }
    }

    fun findTextCommand(words: List<String>): TextFindResult {
        var tree = treeRoot
        var pathComponents = 0
        words.forEach {
            tree.children[it]?.let { nextTree ->
                tree = nextTree
                pathComponents++
            }
        }

        return TextFindResult(pathComponents, tree.getCommands())
    }

    fun findFirstTextCommand(words: List<String>): TextCommandInfo? {
        return findTextCommand(words).commands.firstOrNull()
    }

    fun findFirstTextSubcommands(words: List<String>): List<TextCommandInfo> {
        val tree = words.fold(treeRoot) { t, n ->
            return@fold t.children[n] ?: return emptyList()
        }

        return arrayListOf<TextCommandInfo>().also { getFirstSubcommands(it, tree) }
    }

    private fun getFirstSubcommands(subcommands: MutableList<TextCommandInfo>, tree: TextCommandTree) {
        tree.children.forEach { (_, children) ->
            children.getCommands().firstOrNull()?.let { subcommands += it }

            getFirstSubcommands(subcommands, children)
        }
    }

    fun getFirstRootCommands(): Collection<TextCommandInfo> {
        return treeRoot.children.values.map { it.getCommands().first() }
    }
}