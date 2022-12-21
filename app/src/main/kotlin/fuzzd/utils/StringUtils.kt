package fuzzd.utils // ktlint-disable filename

fun indent(str: String): String =
    str.split("\n").joinToString("\n") { "\t$it" }
