package fuzzd.utils // ktlint-disable filename

fun <T> indent(item: T): String =
    item.toString().split("\n").joinToString("\n") { "\t$it" }

fun String.toHexInt(): Int {
    val negative = this[0] == '-'
    val rest = if (negative) this.substring(1 until length) else this

    val value = if (rest.startsWith("0x")) {
        rest.substring(2).toInt(16)
    } else {
        rest.toInt()
    }

    return if (negative) -1 * value else value
}
