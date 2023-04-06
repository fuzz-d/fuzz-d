package fuzzd.utils

inline fun <T, R> Iterable<T>.foldFromEmpty(operation: (acc: Iterable<R>, T) -> Set<R>): Set<R> =
    this.fold(setOf(), operation)

fun <T> Iterable<Iterable<T>>.unionAll() = this.foldFromEmpty { x, y -> x union y }

fun <T, R> Iterable<Pair<T, List<R>>>.foldPair() = this.fold(Pair(emptyList<T>(), emptyList<R>())) { acc, l ->
    Pair(acc.first + l.first, acc.second + l.second)
}

fun <T> Iterable<Iterable<T>>.reduceLists() = this.reduce { x, y -> x + y }.toList()
