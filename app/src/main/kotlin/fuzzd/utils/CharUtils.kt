package fuzzd.utils

fun Char.escape(): String =
    if (this == '\\' || this == '\'' || this == '\n') {
        "\\${this}"
    } else {
        this.toString()
    }
