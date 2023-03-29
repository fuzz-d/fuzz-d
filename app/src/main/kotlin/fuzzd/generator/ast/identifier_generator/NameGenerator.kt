package fuzzd.generator.ast.identifier_generator

sealed class NameGenerator(private val namePrefix: String) {
    private var count: Int = 0

    fun newValue(): String = "$namePrefix${count++}"

    class IdentifierNameGenerator : NameGenerator("v")

    class LoopCounterGenerator : NameGenerator("i")

    class FunctionMethodNameGenerator : NameGenerator("fm")

    class ParameterNameGenerator : NameGenerator("p")

    class MethodNameGenerator : NameGenerator("m")

    class ReturnsNameGenerator : NameGenerator("r")

    class ClassNameGenerator : NameGenerator("C")

    class TraitNameGenerator : NameGenerator("T")

    class FieldNameGenerator : NameGenerator("f")

    class TemporaryNameGenerator : NameGenerator("t")
}
