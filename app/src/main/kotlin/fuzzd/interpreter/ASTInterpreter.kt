package fuzzd.interpreter

import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.MatchExpressionAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.SequenceComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssertStatementAST
import fuzzd.generator.ast.StatementAST.AssignSuchThatStatement
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.ForLoopAST
import fuzzd.generator.ast.StatementAST.ForallStatementAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.BoolValue
import fuzzd.interpreter.value.Value.CharValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.StringValue

interface ASTInterpreter {
    /* ============================== TOP LEVEL ============================== */
    fun interpretDafny(dafny: DafnyAST): Pair<String, List<StatementAST>>

    fun interpretMainFunction(mainFunction: MainFunctionAST, context: InterpreterContext): List<StatementAST>

    fun interpretSequence(sequence: SequenceAST, context: InterpreterContext)

    /* ============================== STATEMENTS ============================= */

    fun interpretStatement(statement: StatementAST, context: InterpreterContext)

    fun interpretAssertStatement(assertStatement: AssertStatementAST, context: InterpreterContext)

    fun interpretAssignSuchThatStatement(statement: AssignSuchThatStatement, context: InterpreterContext)

    fun interpretMatchStatement(matchStatement: MatchStatementAST, context: InterpreterContext)

    fun interpretIfStatement(ifStatement: IfStatementAST, context: InterpreterContext)

    fun interpretCounterLimitedWhileStatement(whileStatement: CounterLimitedWhileLoopAST, context: InterpreterContext)

    fun interpretForallStatement(forallStatement: ForallStatementAST, context: InterpreterContext)

    fun interpretForLoopStatement(forLoop: ForLoopAST, context: InterpreterContext)

    fun interpretWhileStatement(whileStatement: WhileLoopAST, context: InterpreterContext)

    fun interpretVoidMethodCall(methodCall: VoidMethodCallAST, context: InterpreterContext)

    fun interpretMultiTypedDeclaration(typedDeclaration: MultiTypedDeclarationAST, context: InterpreterContext)

    fun interpretMultiDeclaration(declaration: MultiDeclarationAST, context: InterpreterContext)

    fun interpretMultiAssign(assign: MultiAssignmentAST, context: InterpreterContext)

    fun interpretPrint(printAST: PrintAST, context: InterpreterContext)

    /* ============================= EXPRESSIONS ============================= */

    fun interpretExpression(expression: ExpressionAST, context: InterpreterContext): Value

    fun interpretDatatypeInstantiation(instantiation: DatatypeInstantiationAST, context: InterpreterContext): Value

    fun interpretDatatypeUpdate(update: DatatypeUpdateAST, context: InterpreterContext): Value

    fun interpretDatatypeDestructor(destructor: DatatypeDestructorAST, context: InterpreterContext): Value

    fun interpretMatchExpression(matchExpression: MatchExpressionAST, context: InterpreterContext): Value

    fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST, context: InterpreterContext): Value

    fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST, context: InterpreterContext): Value

    fun interpretClassInstantiation(classInstantiation: ClassInstantiationAST, context: InterpreterContext): Value

    fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST, context: InterpreterContext): Value

    fun interpretTernaryExpression(ternaryExpression: TernaryExpressionAST, context: InterpreterContext): Value

    fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST, context: InterpreterContext): Value

    fun interpretModulus(modulus: ModulusExpressionAST, context: InterpreterContext): Value

    fun interpretMultisetConversion(multisetConversion: MultisetConversionAST, context: InterpreterContext): Value

    fun interpretIdentifier(identifier: IdentifierAST, context: InterpreterContext): Value

    fun interpretIndex(index: IndexAST, context: InterpreterContext): Value

    fun interpretSetDisplay(setDisplay: SetDisplayAST, context: InterpreterContext): Value

    fun interpretSetComprehension(setComprehension: SetComprehensionAST, context: InterpreterContext): Value

    fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST, context: InterpreterContext): Value

    fun interpretSequenceComprehension(sequenceComprehension: SequenceComprehensionAST, context: InterpreterContext): Value

    fun interpretMapConstructor(mapConstructor: MapConstructorAST, context: InterpreterContext): Value

    fun interpretMapComprehension(mapComprehension: MapComprehensionAST, context: InterpreterContext): Value

    fun interpretArrayLength(arrayLength: ArrayLengthAST, context: InterpreterContext): Value

    fun interpretArrayInit(arrayInit: ArrayInitAST, context: InterpreterContext): Value

    fun interpretCharacterLiteral(characterLiteral: CharacterLiteralAST, context: InterpreterContext): CharValue

    fun interpretStringLiteral(stringLiteral: StringLiteralAST, context: InterpreterContext): StringValue

    fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST, context: InterpreterContext): IntValue

    fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST, context: InterpreterContext): BoolValue
}
