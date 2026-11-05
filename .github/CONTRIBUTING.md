[//]: # (most of it is from https://kotlinlang.org/docs/jvm-api-guidelines-introduction.html)

### Commits
Try to have commit titles not too long, and use the commit body to explain your reasoning.

If your PR updates multiple features, or if it could be unclear what features a commit updates, prepend the name of the affected feature, for example: `components: fix [...]`

### Documentation
- Document `@return` on boolean annotations or non-trivial returns (such as `the time in [timeUnit]`)
- Document default values (e.g., for configs)
- If applicable, put DSL equivalent on annotations using `@see`
- If applicable, put annotation equivalent on DSL properties, **only** using `@see`
- Always put `@` in front of linked annotations, such as `@see BService @BService`
- Use `-` for lists

### Readability
- Always specify member visibility (especially internal)
- Always specify return types (at least on API level)

### Predictability
- Use members for core functions, extensions for others
  - Avoid doing so for Java APIs
  - Use this rule especially for Kotlin APIs
- Use `require` to validate parameters, `check` to validate object state
- Avoid booleans if possible
  - When a function can have a similar name without the use of a boolean (like `map(filterNulls = true)` => `mapNotNull`)
- Avoid arrays and varargs
  - Prefer copying into a list 
  - Always copy into a list if passed down to another function

### Debuggability
- Provide `toString()` methods when it makes sense

### Backward compatibility
- Avoid data classes in API
- Return wider types as much without sacrificing functionality, for example, returning a `Collection` instead of a `List` might make sense if random access isn't required, and allows for future updates to return a different collection type without breakages

## Building locally
You can build the library and publish it to your *local* Maven repository by running `./gradlew publishToMavenLocal`,
at which point you can use the library with the build tool of your choice, 
with the artifact `io.github.freya022:BotCommands:$version`, 
where `$version` is the latest version suffixed with `_DEV`.

## Running the test bot

### Additional requirements

* A PostgreSQL database
* Your bot token

### Configuration
Go to `/test-files/test` from the project root, duplicate the `config-template` folder as `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

You can then run the `Main` class of the `test-bot` module.
