package fuzzd.recondition

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
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
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssertStatementAST
import fuzzd.generator.ast.StatementAST.DisjunctiveAssertStatementAST
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
import fuzzd.generator.ast.TopLevelAST

interface ASTReconditioner {
    /* ========================================== TOP LEVEL ============================================ */
    fun recondition(dafnyAST: DafnyAST): DafnyAST

    fun reconditionTopLevel(topLevelAST: TopLevelAST): TopLevelAST

    fun reconditionMainFunction(mainFunction: MainFunctionAST): MainFunctionAST

    fun reconditionSequence(sequence: SequenceAST): SequenceAST

    fun reconditionFunctionMethod(functionMethodAST: FunctionMethodAST): FunctionMethodAST

    fun reconditionMethod(methodAST: MethodAST): MethodAST

    fun reconditionClass(classAST: ClassAST): ClassAST

    /* ========================================== STATEMENTS =========================================== */
    fun reconditionStatement(statement: StatementAST): StatementAST

    fun reconditionAssertStatement(assertStatement: AssertStatementAST): AssertStatementAST

    fun reconditionDisjunctiveAssertStatement(assertStatement: DisjunctiveAssertStatementAST): DisjunctiveAssertStatementAST

    fun reconditionMultiAssignmentAST(multiAssignmentAST: MultiAssignmentAST): MultiAssignmentAST

    fun reconditionMultiTypedDeclarationAST(multiTypedDeclarationAST: MultiTypedDeclarationAST): MultiTypedDeclarationAST

    fun reconditionMultiDeclarationAST(multiDeclarationAST: MultiDeclarationAST): MultiDeclarationAST

    fun reconditionMatchStatement(matchStatement: MatchStatementAST): MatchStatementAST

    fun reconditionIfStatement(ifStatementAST: IfStatementAST): IfStatementAST

    fun reconditionForLoopStatement(forLoopAST: ForLoopAST): ForLoopAST

    fun reconditionForallStatement(forallStatementAST: ForallStatementAST): ForallStatementAST

    fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST): WhileLoopAST

    fun reconditionPrintAST(printAST: PrintAST): PrintAST

    fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST): VoidMethodCallAST

    /* ========================================== EXPRESSIONS ========================================== */
    fun reconditionExpression(expression: ExpressionAST): ExpressionAST

    fun reconditionDatatypeDestructor(destructor: DatatypeDestructorAST): DatatypeDestructorAST

    fun reconditionDatatypeInstantiation(instantiation: DatatypeInstantiationAST): DatatypeInstantiationAST

    fun reconditionDatatypeUpdate(update: DatatypeUpdateAST): DatatypeUpdateAST

    fun reconditionMatchExpression(matchExpression: MatchExpressionAST): MatchExpressionAST

    fun reconditionBinaryExpression(expression: BinaryExpressionAST): ExpressionAST

    fun reconditionUnaryExpression(expression: UnaryExpressionAST): ExpressionAST

    fun reconditionModulusExpression(modulus: ModulusExpressionAST): ModulusExpressionAST

    fun reconditionMultisetConversion(multisetConversion: MultisetConversionAST): MultisetConversionAST

    fun reconditionFunctionMethodCall(functionMethodCall: FunctionMethodCallAST): ExpressionAST

    fun reconditionIdentifier(identifierAST: IdentifierAST): ExpressionAST

    fun reconditionIndex(indexAST: IndexAST): ExpressionAST

    fun reconditionTernaryExpression(ternaryExpression: TernaryExpressionAST): ExpressionAST

    fun reconditionClassInstantiation(classInstantiation: ClassInstantiationAST): ExpressionAST

    fun reconditionArrayInit(arrayInit: ArrayInitAST): ArrayInitAST

    fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST

    fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST

    fun reconditionSetDisplay(setDisplay: SetDisplayAST): ExpressionAST

    fun reconditionSetComprehension(setComprehension: SetComprehensionAST): SetComprehensionAST

    fun reconditionMapConstructor(mapConstructor: MapConstructorAST): MapConstructorAST

    fun reconditionMapComprehension(mapComprehension: MapComprehensionAST): MapComprehensionAST

    fun reconditionSequenceDisplay(sequenceDisplay: SequenceDisplayAST): SequenceDisplayAST

    fun reconditionSequenceComprehension(sequenceComprehension: SequenceComprehensionAST): SequenceComprehensionAST
}
