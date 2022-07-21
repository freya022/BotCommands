package com.freya02.botcommands.internal

import dev.minn.jda.ktx.events.CoroutineEventManager
import net.dv8tion.jda.api.events.ReadyEvent

class StartupEventListener(manager: CoroutineEventManager) {
    private var init = false

    init {
        manager.listener<ReadyEvent> {
            if (!init) {
                synchronized(this) {
                    if (!init) {
                        init = true

                        initBC()
                    }
                }
            }
        }
    }

    private fun initBC() {

    }
}