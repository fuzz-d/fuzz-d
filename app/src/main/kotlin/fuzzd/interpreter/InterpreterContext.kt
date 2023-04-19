package fuzzd.interpreter

import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.ClassValue
import fuzzd.interpreter.value.ValueTable

class InterpreterContext(
    val fields: ValueTable<IdentifierAST, Value> = ValueTable(),
    val functions: ValueTable<FunctionMethodSignatureAST, ExpressionAST> = ValueTable(),
    val methods: ValueTable<MethodSignatureAST, SequenceAST> = ValueTable()
) {
    fun increaseDepth(): InterpreterContext =
        InterpreterContext(ValueTable(fields), functions, methods)

    fun functionCall(classValue: ClassValue) = InterpreterContext(
        ValueTable(classValue.fields),
        ValueTable(functions, classValue.functions),
        ValueTable(methods, classValue.methods)
    )

    fun functionCall(fields: ValueTable<IdentifierAST, Value>) = InterpreterContext(fields, functions, methods)

    fun classField(classFields: ValueTable<IdentifierAST, Value>) =
        InterpreterContext(classFields.withParent(fields), functions, methods)
}
