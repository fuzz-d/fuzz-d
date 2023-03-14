package fuzzd.recondition.visitor

class VisitorSymbolTable<T>(private val parent: VisitorSymbolTable<T>? = null) {
    private val table = mutableMapOf<String, T>()

    fun addEntry(name: String, entry: T) {
        table[name] = entry
    }

    fun hasEntry(name: String): Boolean = table[name] != null || (parent?.hasEntry(name) ?: false)

    fun getEntry(name: String): T {
        if (!hasEntry(name)) throw UnsupportedOperationException("Visitor symbol table for entry {$name} not found")

        return if (table[name] != null) table[name]!! else parent!!.getEntry(name)
    }

    fun clone(): VisitorSymbolTable<T> {
        val cloned = VisitorSymbolTable(parent)

        table.entries.forEach { (name, entry) -> cloned.addEntry(name, entry) }

        return cloned
    }

    fun increaseDepth(): VisitorSymbolTable<T> = VisitorSymbolTable(this)

    fun decreaseDepth(): VisitorSymbolTable<T> {
        if (parent == null) throw UnsupportedOperationException("Can't decrease top level depth")
        return parent
    }

    override fun toString(): String = "{$parent}  $table"
}
