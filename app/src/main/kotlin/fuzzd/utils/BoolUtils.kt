package fuzzd.utils

// b1 <==> b2
fun iff(b1: Boolean, b2: Boolean): Boolean = (!b1 && !b2) || (b1 && b2)

// b1 ==> b2
fun implication(b1: Boolean, b2: Boolean): Boolean = !b1 || b2

// b1 <== b2
fun reverseImplication(b1: Boolean, b2: Boolean): Boolean = !b2 || b1

