package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CommandPath
import net.dv8tion.jda.internal.utils.Checks
import kotlin.math.min

internal class CommandPathImpl internal constructor(
    private val name: String,
    private val group: String?,
    private val subname: String?
) : CommandPath {
    private val path: String
    private val count: Int

    init {
        if (group != null) Checks.notBlank(group, "Subcommand group name")
        if (subname != null) Checks.notBlank(subname, "Subcommand name")

        val components = listOfNotNull(name, group, subname)

        this.path = components.joinToString("/")
        this.count = components.size
    }

    override fun getName(): String = name
    override fun getGroup(): String? = group
    override fun getSubname(): String? = subname

    override fun getNameCount(): Int = count

    override fun getParent(): CommandPath? = when {
        group != null && subname != null -> CommandPath.of(name, group) // /name group sub
        group != null -> CommandPath.of(name)  // /name group
        subname != null -> CommandPath.of(name)  // /name sub
        else -> null // /name
    }


    override fun getFullPath(): String {
        return path
    }

    override fun getLastName(): String = when {
        group != null -> subname!! //If a group exist, a subname exists, otherwise it's a bug.
        subname != null -> subname
        else -> name
    }

    override fun getNameAt(i: Int): String? = when (i) {
        0 -> name
        1 ->
            group ?: // /name group subname
            subname // /name subname
        2 -> subname
        else -> throw IllegalArgumentException("Invalid name count: $i")
    }

    override fun startsWith(o: CommandPath): Boolean {
        if (o.nameCount > nameCount) return false
        for (i in 0 until min(nameCount, o.nameCount)) {
            if (o.getNameAt(i) != getNameAt(i)) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandPathImpl

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return path
    }
}