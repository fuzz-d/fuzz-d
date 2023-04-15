package fuzzd.interpreter

import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.BoolValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.StringValue

interface ASTInterpreter {
    /* ============================== TOP LEVEL ============================== */
    fun interpretDafny(dafny: DafnyAST): String

    fun interpretMainFunction(mainFunction: MainFunctionAST)

    fun interpretSequence(sequence: SequenceAST)

    /* ============================== STATEMENTS ============================= */

    fun interpretIfStatement(ifStatement: IfStatementAST)

    fun interpretWhileStatement(whileStatement: WhileLoopAST)

    fun interpretVoidMethodCall(methodCall: VoidMethodCallAST)

    fun interpretDeclaration(declaration: DeclarationAST)

    fun interpretAssign(assign: AssignmentAST)

    fun interpretPrint(printAST: PrintAST)

    /* ============================= EXPRESSIONS ============================= */

    fun interpretExpression(expression: ExpressionAST): Value

    fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST): Value

    fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST): Value

    fun interpretClassInstantiation(classInstantiation: ClassInstantiationAST): Value

    fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST): Value

    fun interpretTernaryExpression(ternaryExpression: TernaryExpressionAST): Value

    fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST): Value

    fun interpretModulus(modulus: ModulusExpressionAST): Value

    fun interpretMultisetConversion(multisetConversion: MultisetConversionAST): Value

    fun interpretIdentifier(identifier: IdentifierAST): Value

    fun interpretSetDisplay(setDisplay: SetDisplayAST): Value

    fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST): Value

    fun interpretMapConstructor(mapConstructor: MapConstructorAST): Value

    fun interpretArrayLength(arrayLength: ArrayLengthAST): Value

    fun interpretArrayInit(arrayInit: ArrayInitAST): Value

    fun interpretStringLiteral(stringLiteral: StringLiteralAST): StringValue

    fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST): IntValue

    fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST): BoolValue
}
