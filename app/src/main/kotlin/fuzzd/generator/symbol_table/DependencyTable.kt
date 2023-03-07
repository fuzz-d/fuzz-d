package fuzzd.generator.symbol_table

class DependencyTable<T> {
    /* map from element to all elements dependent on it */
    private val dependencies = mutableMapOf<T, MutableSet<T>>()

    fun addDependency(target: T, caller: T) {
        if (!dependencies.containsKey(target)) {
            dependencies[target] = mutableSetOf()
        }

        dependencies[target]!!.add(caller)
    }

    fun canUseDependency(caller: T, target: T): Boolean = if (caller == target) {
        false
    } else {
        val callers = findDependenciesFor(caller)
        target !in callers
    }

    private fun findDependenciesFor(target: T): Set<T> {
        val calls = mutableSetOf<T>()
        val queue = mutableListOf<T>()

        queue.addAll(dependencies[target] ?: listOf())

        while (queue.isNotEmpty()) {
            val caller = queue.removeFirst()

            if (caller !in calls) {
                calls.add(caller)
                queue.addAll(dependencies[caller] ?: listOf())
            }
        }

        return calls
    }
}
