package fuzzd.generator.symbol_table

import fuzzd.generator.ast.MethodAST

class MethodCallTable {
    /* map from a method to all the methods that call it */
    private val callers = mutableMapOf<MethodAST, MutableSet<MethodAST>>()

    fun addCall(target: MethodAST, caller: MethodAST) {
        if (!callers.containsKey(target)) {
            callers[target] = mutableSetOf()
        }

        callers[target]!!.add(caller)
    }

    fun canCall(caller: MethodAST, target: MethodAST): Boolean = if (caller == target) {
        false
    } else {
        val callers = findCallersFor(caller)
        target !in callers
    }

    private fun findCallersFor(target: MethodAST): Set<MethodAST> {
        val calls = mutableSetOf<MethodAST>()
        val queue = mutableListOf<MethodAST>()

        queue.addAll(callers[target] ?: listOf())

        while (queue.isNotEmpty()) {
            val caller = queue.removeAt(0)

            if (caller !in calls) {
                calls.add(caller)
                queue.addAll(callers[caller] ?: listOf())
            }
        }

        return calls
    }
}
