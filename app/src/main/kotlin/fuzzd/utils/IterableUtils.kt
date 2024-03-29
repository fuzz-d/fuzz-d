package fuzzd.utils

import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator

inline fun <T, R> Iterable<T>.foldFromEmpty(operation: (acc: Iterable<R>, T) -> Set<R>): Set<R> =
    this.fold(setOf(), operation)

fun <T> Iterable<Iterable<T>>.unionAll() = this.foldFromEmpty { x, y -> x union y }

fun <T, R> Iterable<Pair<T, List<R>>>.foldPair() = this.fold(Pair(emptyList<T>(), emptyList<R>())) { acc, l ->
    Pair(acc.first + l.first, acc.second + l.second)
}

fun <T> Iterable<Iterable<T>>.reduceLists() = this.reduceOrNull { x, y -> x + y }?.toList() ?: emptyList()

fun <T> Iterable<T>.toMultiset(): Map<T, Int> =
    this.fold(mutableMapOf()) { acc, k -> if (k in acc) acc[k] = acc[k]!! + 1 else acc[k] = 1; acc }

fun <T, U> Iterable<Pair<T, U>>.mapFirst(): List<T> = this.map { (k, _) -> k }

fun Iterable<ExpressionAST>.conjunct(): ExpressionAST = this.reduce { l, r -> BinaryExpressionAST(l, ConjunctionOperator, r) }
