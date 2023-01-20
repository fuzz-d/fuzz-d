package fuzzd.utils

fun Char.escape(): String =
    if (this == '\\' || this == '\'') {
        "\\${this}"
    } else {
        this.toString()
    }
