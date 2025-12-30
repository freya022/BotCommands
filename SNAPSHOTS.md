# Using snapshots

You can use a build of the latest, unreleased changes, either using our repository or with JitPack.

We strongly recommend you to use our repository, if it isn't possible, use JitPack.

## Using our repository

Here you can find builds of the latest commits,
the version is the full hash of the commit you want a build of, with `-SNAPSHOT` appended to it,
for example, for [this commit](https://github.com/freya022/BotCommands/commit/fade34bb9c1b67d778a13ef51425f69102b27a48): `fade34bb9c1b67d778a13ef51425f69102b27a48-SNAPSHOT`.

### Maven
```xml
<repositories>
    <repository>
        <id>reposilite-repository-snapshots</id>
        <name>freya02 repository</name>
        <url>https://repo.freya02.dev/snapshots</url>
    </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>ARTIFACT_ID</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    // ...

    // Ensure that Gradle only searches for our snapshots there
    exclusiveContent {
        forRepository {
            maven("https://repo.freya02.dev/snapshots") {
                mavenContent { snapshotsOnly() }
            }
        }

        filter {
            includeVersionByRegex("""io\.github\.freya022""", ".+", "[a-f0-9]{40}-SNAPSHOT")
        }
    }
}

dependencies {
    implementation("io.github.freya022:ARTIFACT_ID:VERSION")
}
```

## Using JitPack

Alternatively, you can use JitPack to use builds of any commit hash.

### Maven

```xml
<repositories>
    <repository>
        <id>reposilite-repository-snapshots</id>
        <name>freya02 repository</name>
        <url>https://repo.freya02.dev/snapshots</url>
    </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>ARTIFACT_ID</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    // ...

    // Ensure that Gradle only searches for our snapshots there
    exclusiveContent {
        forRepository {
            maven("https://jitpack.io")
        }

        filter {
            includeVersionByRegex("""io\.github\.freya022""", ".+", "[a-f0-9]{10}")
        }
    }
}

dependencies {
    implementation("io.github.freya022:ARTIFACT_ID:VERSION")
}
```

> [!NOTE]
> For submodules, the group ID is `io.github.freya022.BotCommands`, the artifact ID and version stay the same.
