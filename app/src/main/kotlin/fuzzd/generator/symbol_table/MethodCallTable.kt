package fuzzd.generator.symbol_table

import fuzzd.generator.ast.MethodSignatureAST

class MethodCallTable {
    /* map from a method to all the methods that call it */
    private val callers = mutableMapOf<MethodSignatureAST, MutableSet<MethodSignatureAST>>()

    fun addCall(target: MethodSignatureAST, caller: MethodSignatureAST) {
        if (!callers.containsKey(target)) {
            callers[target] = mutableSetOf()
        }

        callers[target]!!.add(caller)
    }

    fun canCall(caller: MethodSignatureAST, target: MethodSignatureAST): Boolean = if (caller == target) {
        false
    } else {
        val callers = findCallersFor(caller)
        target !in callers
    }

    private fun findCallersFor(target: MethodSignatureAST): Set<MethodSignatureAST> {
        val calls = mutableSetOf<MethodSignatureAST>()
        val queue = mutableListOf<MethodSignatureAST>()

        queue.addAll(callers[target] ?: listOf())

        while (queue.isNotEmpty()) {
            val caller = queue.removeFirst()

            if (caller !in calls) {
                calls.add(caller)
                queue.addAll(callers[caller] ?: listOf())
            }
        }

        return calls
    }
}
