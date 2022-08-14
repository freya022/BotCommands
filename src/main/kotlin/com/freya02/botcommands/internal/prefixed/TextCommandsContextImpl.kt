package com.freya02.botcommands.internal.prefixed

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

            if (currentTree.exists(path)) throwUser(commandInfo.method, "Text command with path ${commandInfo.path} already exists")

            currentTree.addCommand(path, commandInfo)
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
}