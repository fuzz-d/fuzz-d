package fuzzd.utils // ktlint-disable filename

fun <T> indent(item: T): String =
    item.toString().split("\n").joinToString("\n") { "\t$it" }
