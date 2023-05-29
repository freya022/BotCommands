package com.freya02.botcommands.othertests

import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class MyCollection<T, R>(collection: Collection<Int>) : Collection<Int> by collection

fun test(): Collection<String> = throw UnsupportedOperationException()
fun testCustom(): MyCollection<User, Member> = throw UnsupportedOperationException()

fun main() {
    println(::test.returnType.collectionElementType)
    println(::testCustom.returnType.collectionElementType)
}