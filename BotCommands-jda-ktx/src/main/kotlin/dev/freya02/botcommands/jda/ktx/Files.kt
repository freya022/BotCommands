package dev.freya02.botcommands.jda.ktx

import net.dv8tion.jda.api.utils.FileUpload

@ReplaceJdaKtx("dev.minn.jda.ktx.messages")
fun FileUpload.into() = listOf(this)
