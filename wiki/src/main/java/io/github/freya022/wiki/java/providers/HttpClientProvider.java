package io.github.freya022.wiki.java.providers;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:http_client_provider-java]
@BService
public class HttpClientProvider {
//    @Primary // This is only needed if you try to get an OkHttpClient without matching the name
    @BService
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }

    @BService
    public OkHttpClient cachedHttpClient(
            // Inject the default http client (declared above)
            // This is not the same as calling the method! as it would create 2 different clients

            // This would not work if the parameter was named differently,
            // unless @Primary was used on the default declaration above
            OkHttpClient httpClient
    ) throws IOException {
        final File tempDirectory = Files.createTempDirectory(null).toFile();
        return httpClient.newBuilder()
                .cache(new Cache(tempDirectory, 1024 * 1024))
                .build();
    }
}
// --8<-- [end:http_client_provider-java]