package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.GlobalExceptionHandler
import com.freya02.botcommands.api.GlobalExceptionHandlerAdapter
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ServiceType
import net.dv8tion.jda.api.events.Event

@BService
@ServiceType(GlobalExceptionHandler::class)
object MyGlobalExceptionHandler : GlobalExceptionHandlerAdapter() {
    override fun handle(context: BContext, event: Event?, throwable: Throwable) {
        super.handle(context, event, throwable)
    }
}