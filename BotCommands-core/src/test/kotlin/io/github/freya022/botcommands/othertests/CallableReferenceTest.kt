package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference

private interface TestInterface {
    fun testInterface()
}

private open class TestSubject : TestInterface {
    fun testSubject() {}

    override fun testInterface() {}
}

private open class TestSubject2 : TestSubject() {
    override fun testInterface() {}
}

object CallableReferenceTest {
    fun test() {}

    @JvmStatic
    fun main(args: Array<String>) {
        val test = ::test
        val testSubject = TestSubject::testSubject
        val instance: TestInterface = TestSubject()
        val instance2: TestInterface = TestSubject2()
        val testInterface = instance::testInterface
        val testInterface2 = instance2::testInterface

        val reflectedTestSubject = testSubject.reflectReference()
        val reflectedTestInterface = testInterface.reflectReference()
        val reflectedTestInterface2 = testInterface2.reflectReference()

        println()
    }
}