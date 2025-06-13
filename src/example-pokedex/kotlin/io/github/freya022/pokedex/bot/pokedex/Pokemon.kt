package io.github.freya022.pokedex.bot.pokedex

class Pokemon(
    val id: Int,
    val name: Name,
    val type: List<String>,
    val species: String,
    val description: String,
    val evolution: Evolution,
) {

    class Name(
        val english: String,
    )

    class Evolution(
        prev: List<String>?,
        next: List<List<String>>?,
    ) {

        val prev: EvolutionCriteria? = prev?.let(::EvolutionCriteria)
        val next: List<EvolutionCriteria>? = next?.map(::EvolutionCriteria)

        class EvolutionCriteria(
            val id: Int,
            val criteria: List<String>,
        ) {

            constructor(data: List<String>) : this(
                data[0].toInt(),
                data.drop(1),
            )
        }
    }
}