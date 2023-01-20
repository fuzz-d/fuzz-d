package fuzzd.generator.ast.identifier_generator

class LoopCounterGenerator() : NameGenerator {
    private var count = 0

    override fun newValue(): String = "i${count++}"
}
