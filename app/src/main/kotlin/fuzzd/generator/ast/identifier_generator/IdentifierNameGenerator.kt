package fuzzd.generator.ast.identifier_generator

class IdentifierNameGenerator(private val parent: IdentifierNameGenerator? = null) : NameGenerator {
    private var count: Int = 0

    override fun newValue(): String = "v${count++}"
}
