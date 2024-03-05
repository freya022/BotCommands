package io.github.freya022.wiki.providers

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.nio.file.Files

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:http_client_provider-kotlin]
@BService
class HttpClientProvider {
//    @Primary // This is only needed if you try to get an OkHttpClient without matching the name
    @BService
    fun httpClient(): OkHttpClient = OkHttpClient()

    @BService
    fun cachedHttpClient(
        // Inject the default http client (declared above)
        // This is not the same as calling the method! as it would create 2 different clients

        // This would not work if the parameter was named differently,
        // unless @Primary was used on the default declaration above
        httpClient: OkHttpClient
    ): OkHttpClient {
        val tempDirectory = Files.createTempDirectory(null).toFile()
        return httpClient.newBuilder()
            .cache(Cache(tempDirectory, maxSize = 1024 * 1024))
            .build()
    }
}
// --8<-- [end:http_client_provider-kotlin]