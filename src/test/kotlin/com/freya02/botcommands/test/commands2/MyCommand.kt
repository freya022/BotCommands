package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration

fun interface Lol {
    fun BContext.xd()
}

class MyCommand {
    fun test(t: Lol) {}

    @Declaration
    fun declare() {
        test {

        }
    }
}