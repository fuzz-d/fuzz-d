package fuzzd.generator

class IdentifierNameGenerator(private val parent: IdentifierNameGenerator? = null) {
    private var count: Int = 0

    fun newIdentifierName(): String = "v${count++}"
}
