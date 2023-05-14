package fuzzd.mutation

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.DisjunctiveAssertStatementAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.VerificationAwareWhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.VerifierAnnotationAST
import fuzzd.generator.ast.VerifierAnnotationAST.EnsuresAnnotation
import fuzzd.generator.ast.VerifierAnnotationAST.InvariantAnnotation
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.selection.SelectionManager

class VerifierAnnotationMutator(val selectionManager: SelectionManager) {
    private var mutationUsed = false

    private fun <T> mutate(element: T, block: () -> T): T = if (mutationUsed) element else block()

    fun mutateDafny(dafny: DafnyAST): DafnyAST = DafnyAST(dafny.topLevelElements.map(this::mutateTopLevel))

    fun mutateTopLevel(topLevel: TopLevelAST): TopLevelAST = mutate(topLevel) {
        when (topLevel) {
            is MainFunctionAST -> MainFunctionAST(mutateSequence(topLevel.sequenceAST))
            is MethodAST -> mutateMethod(topLevel)
            is ClassAST -> mutateClass(topLevel)
            else -> topLevel
        }
    }

    fun mutateClass(clazz: ClassAST): ClassAST = ClassAST.builder()
        .withName(clazz.name)
        .withExtends(clazz.extends)
        .withFunctionMethods(clazz.functionMethods)
        .withMethods(clazz.methods.map(this::mutateMethod).toSet())
        .withFields(clazz.fields)
        .withInheritedFields(clazz.inheritedFields)
        .build()

    fun mutateMethod(method: MethodAST): MethodAST = MethodAST(method.signature, mutateSequence(method.getBody()))

    fun mutateSequence(sequence: SequenceAST): SequenceAST = mutate(sequence) {
        if (!sequence.isLive()) {
            sequence
        } else {
            SequenceAST(sequence.statements.map(this::mutateStatement))
        }
    }

    fun mutateStatement(statement: StatementAST): StatementAST = mutate(statement) {
        when (statement) {
            is DisjunctiveAssertStatementAST -> mutateDisjunctiveAssertStatement(statement)
            is VerificationAwareWhileLoopAST -> mutateVerificationAwareWhileLoop(statement)
            is IfStatementAST -> IfStatementAST(statement.condition, mutateSequence(statement.ifBranch), statement.elseBranch?.let { mutateSequence(it) })
            is MatchStatementAST -> MatchStatementAST(statement.match, statement.cases.map { Pair(it.first, mutateSequence(it.second)) })
            else -> statement
        }
    }

    fun mutateDisjunctiveAssertStatement(assertStatement: DisjunctiveAssertStatementAST): DisjunctiveAssertStatementAST = mutate(assertStatement) {
        if (assertStatement.exprs.isNotEmpty()) {
            DisjunctiveAssertStatementAST(assertStatement.baseExpr, assertStatement.exprs.map(this::mutateExpression).toMutableList())
        } else {
            DisjunctiveAssertStatementAST(mutateExpression(assertStatement.baseExpr), mutableListOf())
        }
    }

    fun mutateVerificationAwareWhileLoop(whileLoop: VerificationAwareWhileLoopAST): VerificationAwareWhileLoopAST =
        VerificationAwareWhileLoopAST(
            whileLoop.counter,
            whileLoop.modset,
            whileLoop.counterInitialisation,
            whileLoop.terminationCheck,
            whileLoop.counterUpdate,
            whileLoop.condition,
            whileLoop.decreases,
            whileLoop.invariants.map(this::mutateInvariantAnnotation).toMutableList(),
            mutateSequence(whileLoop.body),
        )

    fun mutateAnnotation(annotation: VerifierAnnotationAST): VerifierAnnotationAST = mutate(annotation) {
        when (annotation) {
            is EnsuresAnnotation -> mutateEnsuresAnnotation(annotation)
            is InvariantAnnotation -> mutateInvariantAnnotation(annotation)
            else -> annotation
        }
    }

    fun mutateInvariantAnnotation(invariantAnnotation: InvariantAnnotation): InvariantAnnotation = mutate(invariantAnnotation) {
        InvariantAnnotation(mutateExpression(invariantAnnotation.expr))
    }

    fun mutateEnsuresAnnotation(ensuresAnnotation: EnsuresAnnotation): EnsuresAnnotation = mutate(ensuresAnnotation) {
        EnsuresAnnotation(mutateExpression(ensuresAnnotation.expr))
    }

    // take an assert statement of form ((x1 == a1) && (x2 == a2) && ... ==>)? (
    fun mutateExpression(expression: ExpressionAST): ExpressionAST = mutate(expression) {
        if (expression is BinaryExpressionAST) {
            when {
                expression.operator is EqualsOperator && selectionManager.selectMutateVerificationCondition() -> {
                    mutationUsed = true
                    if (selectionManager.selectMutateAssertFalse()) {
                        BooleanLiteralAST(false)
                    } else {
                        BinaryExpressionAST(expression.expr1, NotEqualsOperator, expression.expr2)
                    }
                }

                expression.operator is ImplicationOperator -> BinaryExpressionAST(expression.expr1, ImplicationOperator, mutateExpression(expression.expr2))
                expression.operator is ConjunctionOperator -> BinaryExpressionAST(mutateExpression(expression.expr1), ConjunctionOperator, mutateExpression(expression.expr2))

                else -> expression
            }
        } else {
            expression
        }
    }
}
