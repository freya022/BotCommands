package dev.freya02.botcommands.spring.metadata.generator.data

import kotlinx.serialization.Serializable

@Serializable
internal class SpringMetadata(
    val groups: MutableList<GroupMetadata>,
    val properties: MutableList<PropertyMetadata>,
    val hints: MutableList<ClassReferenceHint>,
) {
    constructor() : this(arrayListOf(), arrayListOf(), arrayListOf())
}

@Serializable
internal class GroupMetadata(
    val name: String,
    val type: String,
    val sourceType: String,
)

@Serializable
internal class PropertyMetadata(
    val name: String,
    val defaultValue: String?,
    val type: String,
    val sourceType: String,
    val description: String,
    val deprecation: Deprecation?
) {

    @Serializable
    class Deprecation(
        val reason: String,
        val level: String,
        val replacement: String?,
    )
}

@Serializable
internal class ClassReferenceHint(val name: String, val providers: List<Provider>) {

    @Serializable
    class Provider(val name: String, val parameters: Parameters) {

        @Serializable
        class Parameters(val target: String)
    }

    constructor(name: String, targetClass: String) : this(name, listOf(Provider("class-reference", Provider.Parameters(targetClass))))
}
