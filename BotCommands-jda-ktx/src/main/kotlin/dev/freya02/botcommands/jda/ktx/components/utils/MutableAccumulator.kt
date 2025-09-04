package dev.freya02.botcommands.jda.ktx.components.utils

class MutableAccumulator<T>(val collection: MutableCollection<T>) {

    operator fun T.unaryPlus() {
        collection += this
    }

    operator fun Collection<T>.unaryPlus() {
        collection += this
    }

    operator fun plusAssign(collection: Collection<T>) {
        this.collection += collection
    }

    operator fun plusAssign(item: T) {
        this.collection += collection
    }
}
