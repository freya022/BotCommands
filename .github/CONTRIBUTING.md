[//]: # (most of it is from https://kotlinlang.org/docs/jvm-api-guidelines-introduction.html)

### Commits
Wiki commits must be with `Wiki: Commit text`
Examples commits must be with `Examples: Commit text`

If these commits are in a PR, the commits can omit this format, 
but the PR merge commit must contain the prefix.

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
- Return the best type; don't narrow

## Building locally
You can build the library and publish it to your *local* Maven repository by running `mvn install`,
at which point you can use the library with the build tool of your choice, 
with the artifact `io.github.freya022:BotCommands:$version`, 
where `$version` is the version in the `pom.xml` suffixed with `_DEV`.

## Running the test bot

### Additional requirements

* A PostgreSQL database
* Your bot token

### Configuration
In the project root, duplicate the `config-template` folder as `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

You can then run the `Main` class of the `test` package.