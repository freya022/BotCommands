package dev.freya02.botcommands.plugins

import Version
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.provideDelegate

abstract class PublishedProjectEnvironmentConfig(
    val project: Project
) {
    abstract val version: Property<Version>

    val mavenCentralUsername: String? by project
    val mavenCentralPassword: String? by project
    /**
     * 1. Generate a key pair with `gpg --gen-key`, it will ask for a key name and an email address
     * 2. Start editing the key with `gpg --edit-key <key name>`, you should see a `gpg>` prompt
     * 3. (Optional) Modify the expiry of your primary key with `expire`
     * 4. If a subkey (`ssb`) was generated automatically, you can delete it by selecting it with `key 1` and running `delkey`
     * 5. Add a subkey with `addkey`, select `RSA (sign only)`
     * 6. Save everything with `save`, it will exit the gpg prompt
     * 7. Show the subkey IDs with `gpg -K --keyid-format short`, you should see two keys:
     *    - `sec ed25519/<primary key id> <created on> [SC]`, with the line below being the public key
     *    - `ssb rsa<length>/<subkey id> <created on> [S]`
     * 8. Set `mavenGpgKeyId` with the subkey id
     * 9. Set `mavenGpgSecretKey` with the secret key using `gpg --export-secret-key --armor <public key>`
     */
    val mavenGpgKeyId: String? by project
    val mavenGpgSecretKey: String? by project

    val reposiliteUsername: String? by project
    val reposilitePassword: String? by project

    val canSign = mavenGpgKeyId != null && mavenGpgSecretKey != null
    val canPublish = mavenCentralUsername != null && mavenCentralPassword != null && canSign
    val canPublishSnapshot = reposiliteUsername?.isNotBlank() == true && reposilitePassword?.isNotBlank() == true && canSign

    val effectiveTag = if (canPublish) "v${version}" else "3.X"
}
