package fuzzd.recondition

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
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
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.identifier_generator.NameGenerator.SafetyIdGenerator
import fuzzd.generator.ast.operators.BinaryOperator.MathematicalBinaryOperator
import fuzzd.logging.Logger
import fuzzd.utils.SAFE_ARRAY_INDEX
import fuzzd.utils.safetyMap

class Reconditioner(private val logger: Logger, private val ids: Set<String>? = null) : ASTReconditioner {
    private fun requiresSafety(str: String) = ids == null || str in ids

    private val safetyIdGenerator = SafetyIdGenerator()
    val idsMap = mutableMapOf<String, ASTElement>()

    override fun recondition(dafnyAST: DafnyAST): DafnyAST {
        // needs to recondition in the same order as Advanced Reconditioning
        val reconditionedTraits = dafnyAST.topLevelElements.filterIsInstance<TraitAST>()
        val reconditionedClasses = dafnyAST.topLevelElements.filterIsInstance<ClassAST>().map(this::reconditionClass)
        val reconditionedMethods = dafnyAST.topLevelElements.filterIsInstance<MethodAST>().map(this::reconditionMethod)
        val reconditionedFunctionMethods =
            dafnyAST.topLevelElements.filterIsInstance<FunctionMethodAST>().map(this::reconditionFunctionMethod)
        val reconditionedMain =
            reconditionMainFunction(dafnyAST.topLevelElements.first { it is MainFunctionAST } as MainFunctionAST)

        return DafnyAST(
            reconditionedTraits +
                reconditionedClasses +
                reconditionedFunctionMethods +
                reconditionedMethods +
                reconditionedMain,
        )
    }

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
        methodAST.setBody(reconditionSequence(methodAST.getBody()))
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
        is MultiTypedDeclarationAST -> reconditionMultiTypedDeclarationAST(statement)
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

    override fun reconditionMultiTypedDeclarationAST(multiTypedDeclarationAST: MultiTypedDeclarationAST) =
        MultiTypedDeclarationAST(
            multiTypedDeclarationAST.identifiers.map(this::reconditionIdentifier),
            multiTypedDeclarationAST.exprs.map(this::reconditionExpression),
        )

    override fun reconditionMultiDeclarationAST(multiDeclarationAST: MultiDeclarationAST) = MultiDeclarationAST(
        multiDeclarationAST.identifiers.map(this::reconditionIdentifier),
        multiDeclarationAST.exprs.map(this::reconditionExpression),
    )

    override fun reconditionIfStatement(ifStatementAST: IfStatementAST) = IfStatementAST(
        reconditionExpression(ifStatementAST.condition),
        reconditionSequence(ifStatementAST.ifBranch),
        ifStatementAST.elseBranch?.let(this::reconditionSequence),
    )

    override fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST) = when (whileLoopAST) {
        is CounterLimitedWhileLoopAST -> CounterLimitedWhileLoopAST(
            whileLoopAST.counterInitialisation,
            whileLoopAST.terminationCheck,
            whileLoopAST.counterUpdate,
            reconditionExpression(whileLoopAST.condition),
            reconditionSequence(whileLoopAST.body),
        )

        else -> WhileLoopAST(
            reconditionExpression(whileLoopAST.condition),
            reconditionSequence(whileLoopAST.body),
        )
    }

    override fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST) = VoidMethodCallAST(
        voidMethodCallAST.method,
        voidMethodCallAST.params.map(this::reconditionExpression),
    )

    override fun reconditionPrintAST(printAST: PrintAST) = PrintAST(printAST.expr.map { reconditionExpression(it) })

    override fun reconditionExpression(expression: ExpressionAST): ExpressionAST = when (expression) {
        is BinaryExpressionAST -> reconditionBinaryExpression(expression)
        is UnaryExpressionAST -> reconditionUnaryExpression(expression)
        is TernaryExpressionAST -> reconditionTernaryExpression(expression)
        is IdentifierAST -> reconditionIdentifier(expression)
        is LiteralAST, is ArrayInitAST -> expression // don't need to do anything
        is ClassInstantiationAST -> reconditionClassInstantiation(expression)
        is ArrayLengthAST -> reconditionArrayLengthAST(expression)
        is NonVoidMethodCallAST -> reconditionNonVoidMethodCallAST(expression)
        is FunctionMethodCallAST -> reconditionFunctionMethodCall(expression)
        else -> throw UnsupportedOperationException() // TODO ??
    }

    override fun reconditionBinaryExpression(expression: BinaryExpressionAST): ExpressionAST {
        val rexpr1 = reconditionExpression(expression.expr1)
        val rexpr2 = reconditionExpression(expression.expr2)

        return if (expression.operator is MathematicalBinaryOperator &&
            safetyMap.containsKey(Pair(expression.operator, expression.type()))
        ) {
            val safetyId = safetyIdGenerator.newValue()
            idsMap[safetyId] = expression
            if (requiresSafety(safetyId)) {
                if (ids != null) {
                    logger.log { "$safetyId: Advanced reconditioning requires safety for binary expression $expression" }
                }

                FunctionMethodCallAST(
                    safetyMap[
                        Pair(
                            expression.operator,
                            expression.type(),
                        ),
                    ]!!.signature,
                    listOf(rexpr1, rexpr2),
                )
            } else {
                BinaryExpressionAST(rexpr1, expression.operator, rexpr2)
            }
        } else {
            BinaryExpressionAST(rexpr1, expression.operator, rexpr2)
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
        is ArrayIndexAST -> {
            val safetyId = safetyIdGenerator.newValue()
            idsMap[safetyId] = identifierAST
            if (requiresSafety(safetyId)) {
                // log advanced reconditioning applications
                if (ids != null) {
                    logger.log { "$safetyId: Advanced reconditioning requires safety for array index $identifierAST" }
                }

                ArrayIndexAST(
                    identifierAST.array,
                    FunctionMethodCallAST(
                        SAFE_ARRAY_INDEX.signature,
                        listOf(identifierAST.index, ArrayLengthAST(identifierAST.array)),
                    ),
                )
            } else {
                identifierAST
            }
        }

        is ClassInstanceFieldAST -> ClassInstanceFieldAST(
            reconditionIdentifier(identifierAST.classInstance),
            reconditionIdentifier(identifierAST.classField),
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

    override fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST =
        ArrayLengthAST(reconditionIdentifier(arrayLengthAST.array))

    override fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST =
        NonVoidMethodCallAST(
            nonVoidMethodCall.method,
            nonVoidMethodCall.params.map(this::reconditionExpression),
        )
}
