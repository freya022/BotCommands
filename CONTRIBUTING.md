[//]: # (most of it is from https://kotlinlang.org/docs/jvm-api-guidelines-introduction.html)

### Documentation
- Document `@return` on boolean annotations or non-trivial returns (such as `the time in [timeUnit]`)
- Document default values (e.g., for configs)
- Always put DSL equivalent on annotations using `@see`
- Always put annotation equivalent on DSL properties, **only** using `@see`
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
- Provide toString() methods when it makes sense

### Backward compatibility
- Avoid data classes in API
- Return best type - don't narrow