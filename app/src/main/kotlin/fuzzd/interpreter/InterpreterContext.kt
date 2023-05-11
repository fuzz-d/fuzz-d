package fuzzd.interpreter

import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.ValueTable

class InterpreterContext(
    val fields: ValueTable<IdentifierAST, Value> = ValueTable(),
    val functions: ValueTable<FunctionMethodSignatureAST, ExpressionAST> = ValueTable(),
    val methods: ValueTable<MethodSignatureAST, SequenceAST> = ValueTable(),
    val classContext: InterpreterContext? = null,
    val methodContext: MethodSignatureAST? = null,
) {
    fun increaseDepth(): InterpreterContext = InterpreterContext(ValueTable(fields), functions, methods, classContext, methodContext)

    fun withClassContext(classContext: InterpreterContext?) = InterpreterContext(fields, functions, methods, classContext, methodContext)
}
