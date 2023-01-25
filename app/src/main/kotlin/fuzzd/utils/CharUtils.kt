package fuzzd.utils

fun Char.escape(): String =
    if (this == '\\' || this == '\'' || this == '\n') {
        "a"
    } else {
        this.toString()
    }
