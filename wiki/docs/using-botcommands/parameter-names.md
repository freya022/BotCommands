# Enabling Java parameter names

!!! tip

    This is **NOT** required if you are using Kotlin.

A significant part of the framework benefits from having accessible method parameter names, 
such as `#!java @TextOption` or `#!java @SlashOption`, injecting a service by its name, 
including better debugging / error messages.

=== "Maven"

    You can add the necessary compiler argument in the configuration for the Maven compiler plugin:
    ```xml title="pom.xml"
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${compiler.plugin.version}</version>
        <configuration>
            <!-- ... -->
            <compilerArgs>
                <compilerArg>-parameters</compilerArg>
            </compilerArgs>
        </configuration>
    </plugin>
    ```

=== "Gradle"

    You can add the necessary compiler argument to the Java compile options:
    ```kotlin title="build.gradle.kts"
    tasks.withType<JavaCompile> {
        options.compilerArgs += "-parameters"
    }
    ```