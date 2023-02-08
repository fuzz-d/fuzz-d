package fuzzd.utils

inline fun <T, R> Iterable<T>.foldFromEmpty(operation: (acc: Iterable<R>, T) -> Set<R>): Set<R> =
    this.fold(setOf(), operation)

fun <T> Iterable<Iterable<T>>.unionAll() = this.foldFromEmpty { x, y -> x union y }
