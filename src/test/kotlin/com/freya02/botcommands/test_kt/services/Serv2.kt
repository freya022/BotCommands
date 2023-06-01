package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.service.annotations.BService

@BService
class Serv2 private constructor() {
    companion object {
        operator fun invoke() = Serv2()
    }
}
