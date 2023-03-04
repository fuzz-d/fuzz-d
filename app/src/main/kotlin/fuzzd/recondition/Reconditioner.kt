package fuzzd.recondition

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.operators.BinaryOperator.MathematicalBinaryOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.utils.safetyMap

class Reconditioner : ASTReconditioner {
    override fun recondition(dafnyAST: DafnyAST): DafnyAST =
        DafnyAST(dafnyAST.topLevelElements.map(this::reconditionTopLevel))

    override fun reconditionTopLevel(topLevelAST: TopLevelAST) = when (topLevelAST) {
        is ClassAST -> reconditionClass(topLevelAST)
        is TraitAST -> topLevelAST // no reconditioning required
        is MethodAST -> reconditionMethod(topLevelAST)
        is FunctionMethodAST -> reconditionFunctionMethod(topLevelAST)
        is MainFunctionAST -> reconditionMainFunction(topLevelAST)
        else -> throw UnsupportedOperationException()
    }

    override fun reconditionClass(classAST: ClassAST): ClassAST {
        val reconditionedMethods = classAST.methods.map(this::reconditionMethod).toSet()
        val reconditionedFunctionMethods = classAST.functionMethods.map(this::reconditionFunctionMethod).toSet()

        return ClassAST(
            classAST.name,
            classAST.extends,
            reconditionedFunctionMethods,
            reconditionedMethods,
            classAST.fields,
            classAST.inheritedFields,
        )
    }

    override fun reconditionMethod(methodAST: MethodAST): MethodAST {
        methodAST.setBody(reconditionSequence(methodAST.body()))
        return methodAST
    }

    override fun reconditionMainFunction(mainFunction: MainFunctionAST) =
        MainFunctionAST(reconditionSequence(mainFunction.sequenceAST))

    override fun reconditionFunctionMethod(functionMethodAST: FunctionMethodAST) =
        FunctionMethodAST(functionMethodAST.signature, reconditionExpression(functionMethodAST.body))

    override fun reconditionSequence(sequence: SequenceAST): SequenceAST =
        SequenceAST(sequence.statements.map(this::reconditionStatement))

    override fun reconditionStatement(statement: StatementAST) = when (statement) {
        is BreakAST -> statement
        is MultiAssignmentAST -> reconditionMultiAssignmentAST(statement) // covers AssignmentAST
        is MultiDeclarationAST -> reconditionMultiDeclarationAST(statement) // covers DeclarationAST
        is IfStatementAST -> reconditionIfStatement(statement)
        is WhileLoopAST -> reconditionWhileLoopAST(statement)
        is PrintAST -> reconditionPrintAST(statement)
        is VoidMethodCallAST -> reconditionVoidMethodCall(statement)
    }

    override fun reconditionMultiAssignmentAST(multiAssignmentAST: MultiAssignmentAST) = MultiAssignmentAST(
        multiAssignmentAST.identifiers.map(this::reconditionIdentifier),
        multiAssignmentAST.exprs.map(this::reconditionExpression),
    )

    override fun reconditionMultiDeclarationAST(multiDeclarationAST: MultiDeclarationAST) = MultiDeclarationAST(
        multiDeclarationAST.identifiers.map(this::reconditionIdentifier),
        multiDeclarationAST.exprs.map(this::reconditionExpression),
    )

    override fun reconditionIfStatement(ifStatementAST: IfStatementAST) =
        IfStatementAST(
            reconditionExpression(ifStatementAST.condition),
            reconditionSequence(ifStatementAST.ifBranch),
            ifStatementAST.elseBranch?.run(this::reconditionSequence),
        )

    override fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST) = WhileLoopAST(
        whileLoopAST.counterInitialisation,
        whileLoopAST.terminationCheck,
        reconditionExpression(whileLoopAST.condition),
        reconditionSequence(whileLoopAST.body),
        whileLoopAST.counterUpdate,
    )

    override fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST) = VoidMethodCallAST(
        voidMethodCallAST.method,
        voidMethodCallAST.params.map(this::reconditionExpression),
    )

    override fun reconditionPrintAST(printAST: PrintAST) = PrintAST(reconditionExpression(printAST.expr))

    override fun reconditionExpression(expression: ExpressionAST): ExpressionAST = when (expression) {
        is BinaryExpressionAST -> reconditionBinaryExpression(expression)
        is UnaryExpressionAST -> reconditionUnaryExpression(expression)
        is TernaryExpressionAST -> reconditionTernaryExpression(expression)
        is IdentifierAST -> reconditionIdentifier(expression)
        is LiteralAST -> expression // don't need to do anything
        is ClassInstantiationAST -> reconditionClassInstantiation(expression)
        is ArrayInitAST -> reconditionArrayInitialisation(expression)
        is ArrayLengthAST -> reconditionArrayLengthAST(expression)
        is NonVoidMethodCallAST -> reconditionNonVoidMethodCallAST(expression)
        is FunctionMethodCallAST -> reconditionFunctionMethodCall(expression)
    }

    override fun reconditionBinaryExpression(expression: BinaryExpressionAST): ExpressionAST {
        val rexpr1 = reconditionExpression(expression.expr1)
        val rexpr2 = reconditionExpression(expression.expr2)

        return when (expression.operator) {
            is MathematicalBinaryOperator -> FunctionMethodCallAST(
                safetyMap[
                    Pair(
                        expression.operator,
                        expression.type(),
                    ),
                ]!!.signature,
                listOf(rexpr1, rexpr2),
            )

            else -> BinaryExpressionAST(rexpr1, expression.operator, rexpr2)
        }
    }

    override fun reconditionUnaryExpression(expression: UnaryExpressionAST): ExpressionAST =
        UnaryExpressionAST(
            reconditionExpression(expression.expr),
            expression.operator,
        )

    override fun reconditionFunctionMethodCall(functionMethodCall: FunctionMethodCallAST): ExpressionAST =
        FunctionMethodCallAST(
            functionMethodCall.function,
            functionMethodCall.params.map(this::reconditionExpression),
        )

    override fun reconditionIdentifier(identifierAST: IdentifierAST): IdentifierAST = when (identifierAST) {
        is ArrayIndexAST -> ArrayIndexAST(
            identifierAST.array,
            BinaryExpressionAST(identifierAST.index, ModuloOperator, ArrayLengthAST(identifierAST.array)),
        )

        else -> identifierAST
    }

    override fun reconditionTernaryExpression(ternaryExpression: TernaryExpressionAST): ExpressionAST =
        TernaryExpressionAST(
            reconditionExpression(ternaryExpression.condition),
            reconditionExpression(ternaryExpression.ifBranch),
            reconditionExpression(ternaryExpression.elseBranch),
        )

    override fun reconditionClassInstantiation(classInstantiation: ClassInstantiationAST): ExpressionAST =
        ClassInstantiationAST(
            classInstantiation.clazz,
            classInstantiation.params.map(this::reconditionExpression),
        )

    override fun reconditionArrayInitialisation(arrayInit: ArrayInitAST): ExpressionAST = arrayInit

    override fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST =
        ArrayLengthAST(reconditionIdentifier(arrayLengthAST.array))

    override fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST =
        NonVoidMethodCallAST(
            nonVoidMethodCall.method,
            nonVoidMethodCall.params.map(this::reconditionExpression),
        )
}
