package com.freya02.botcommands.othertests

import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class MyCollection<T, R>(collection: Collection<Int>) : Collection<Int> by collection

fun test(): Collection<String> = TODO()
fun testCustom(): MyCollection<User, Member> = TODO()

fun main() {
    println(::test.collectionElementType)
    println(::testCustom.collectionElementType)
}