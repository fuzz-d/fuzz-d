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
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.BoolValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.StringValue
import fuzzd.interpreter.value.ValueTable

interface ASTInterpreter {
    /* ============================== TOP LEVEL ============================== */
    fun interpretDafny(dafny: DafnyAST): Pair<String, List<StatementAST>>

    fun interpretMainFunction(mainFunction: MainFunctionAST): List<StatementAST>

    fun interpretSequence(sequence: SequenceAST, valueTable: ValueTable)

    /* ============================== STATEMENTS ============================= */

    fun interpretStatement(statement: StatementAST, valueTable: ValueTable)

    fun interpretIfStatement(ifStatement: IfStatementAST, valueTable: ValueTable)

    fun interpretWhileStatement(whileStatement: WhileLoopAST, valueTable: ValueTable)

    fun interpretVoidMethodCall(methodCall: VoidMethodCallAST, valueTable: ValueTable)

    fun interpretMultiTypedDeclaration(typedDeclaration: MultiTypedDeclarationAST, valueTable: ValueTable)

    fun interpretMultiDeclaration(declaration: MultiDeclarationAST, valueTable: ValueTable)

    fun interpretMultiAssign(assign: MultiAssignmentAST, valueTable: ValueTable)

    fun interpretPrint(printAST: PrintAST, valueTable: ValueTable)

    /* ============================= EXPRESSIONS ============================= */

    fun interpretExpression(expression: ExpressionAST, valueTable: ValueTable): Value

    fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST, valueTable: ValueTable): Value

    fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST, valueTable: ValueTable): Value

    fun interpretClassInstantiation(classInstantiation: ClassInstantiationAST, valueTable: ValueTable): Value

    fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST, valueTable: ValueTable): Value

    fun interpretTernaryExpression(ternaryExpression: TernaryExpressionAST, valueTable: ValueTable): Value

    fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST, valueTable: ValueTable): Value

    fun interpretModulus(modulus: ModulusExpressionAST, valueTable: ValueTable): Value

    fun interpretMultisetConversion(multisetConversion: MultisetConversionAST, valueTable: ValueTable): Value

    fun interpretIdentifier(identifier: IdentifierAST, valueTable: ValueTable): Value

    fun interpretSetDisplay(setDisplay: SetDisplayAST, valueTable: ValueTable): Value

    fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST, valueTable: ValueTable): Value

    fun interpretMapConstructor(mapConstructor: MapConstructorAST, valueTable: ValueTable): Value

    fun interpretArrayLength(arrayLength: ArrayLengthAST, valueTable: ValueTable): Value

    fun interpretArrayInit(arrayInit: ArrayInitAST, valueTable: ValueTable): Value

    fun interpretStringLiteral(stringLiteral: StringLiteralAST, valueTable: ValueTable): StringValue

    fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST, valueTable: ValueTable): IntValue

    fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST, valueTable: ValueTable): BoolValue
}
