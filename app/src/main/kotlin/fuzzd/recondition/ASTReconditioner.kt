package fuzzd.recondition

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import java.security.DrbgParameters.Instantiation

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

    fun reconditionMultiAssignmentAST(multiAssignmentAST: MultiAssignmentAST): MultiAssignmentAST

    fun reconditionMultiDeclarationAST(multiDeclarationAST: MultiDeclarationAST): MultiDeclarationAST

    fun reconditionIfStatement(ifStatementAST: IfStatementAST): IfStatementAST

    fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST): WhileLoopAST

    fun reconditionPrintAST(printAST: PrintAST): PrintAST

    fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST): VoidMethodCallAST

    /* ========================================== EXPRESSIONS ========================================== */
    fun reconditionExpression(expression: ExpressionAST): ExpressionAST

    fun reconditionBinaryExpression(expression: BinaryExpressionAST): ExpressionAST

    fun reconditionUnaryExpression(expression: UnaryExpressionAST): ExpressionAST

    fun reconditionFunctionMethodCall(functionMethodCall: FunctionMethodCallAST): ExpressionAST

    fun reconditionIdentifier(identifierAST: IdentifierAST): ExpressionAST

    fun reconditionTernaryExpression(ternaryExpression: TernaryExpressionAST): ExpressionAST

    fun reconditionClassInstantiation(classInstantiation: ClassInstantiationAST): ExpressionAST

    fun reconditionArrayInitialisation(arrayInit: ArrayInitAST): ExpressionAST

    fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST

    fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST
}