# Inferred option names

For annotations such as `#!java @TextOption` or `#!java @AppOption`, you would need to set a `name` attribute on them, 
so it can be displayed on Discord

Fortunately, you can make the library take the name of your parameter as the name for your option, 
all you have to do is to tell your compiler(s) to put the parameter metadata in the compiled classes

This should make your code a bit cleaner

## Adding Java & Kotlin parameters for Maven
### Java
You can add this in your `configuration` tag:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${compiler.plugin.version}</version>
    <configuration>
        <source>${java.version}</source>
        <target>${java.version}</target>
        <compilerArgs> <!-- Add this -->
            <compilerArg>-parameters</compilerArg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Kotlin
You can add this in your `configuration` tag:
```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <executions>...</executions>
    <configuration>
        <args> <!-- Add this -->
            <arg>-java-parameters</arg>
            ...
        </args>
    </configuration>
</plugin>
```

## Adding Java & Kotlin parameters for Gradle
### Java
You can add this in your `build.gradle`:
```gradle
compileJava {
    compilerArgs += '-parameters'
}
```

### Kotlin
You can add this in your `build.gradle`:
```gradle
compileKotlin {
    kotlinOptions.javaParameters = true
}
```