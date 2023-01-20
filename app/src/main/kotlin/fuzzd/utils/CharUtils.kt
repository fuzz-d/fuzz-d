package fuzzd.utils

fun Char.escape(): String =
    if (code == 39 || code == 92) {
        "\\${this}"
    } else {
        this.toString()
    }
