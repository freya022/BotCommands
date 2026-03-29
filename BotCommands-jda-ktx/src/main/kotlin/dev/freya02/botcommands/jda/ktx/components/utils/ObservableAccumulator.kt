package dev.freya02.botcommands.jda.ktx.components.utils

class ObservableAccumulator<T : Any>(initialValues: Collection<T>, private val onUpdate: (newItems: List<T>) -> Unit) {

    private val items = initialValues.toMutableList()

    operator fun T.unaryPlus() {
        items += this
        onUpdate(items)
    }

    operator fun Collection<T>.unaryPlus() {
        if (isEmpty()) return
        items += this
        onUpdate(items)
    }

    operator fun plusAssign(collection: Collection<T>) {
        if (collection.isEmpty()) return
        this.items += collection
        onUpdate(items)
    }

    operator fun plusAssign(item: T) {
        this.items += item
        onUpdate(items)
    }

    fun clear() {
        items.clear()
    }

    fun setAll(items: List<T>) {
        clear()
        this += items
        onUpdate(items)
    }

    fun setAll(vararg items: T) {
        clear()
        this.items += items
        onUpdate(this.items)
    }
}
