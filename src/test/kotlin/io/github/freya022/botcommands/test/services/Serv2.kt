package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.BService

@BService
class Serv2 private constructor() {
    companion object {
        operator fun invoke() = Serv2()
    }
}
