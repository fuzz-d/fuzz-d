package fuzzd.recondition

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.TypedDeclarationAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.identifier_generator.NameGenerator.TemporaryNameGenerator

class AdvancedReconditioner {
    private val methodSignatures = mutableMapOf<String, MethodSignatureAST>()
    private val tempGenerator = TemporaryNameGenerator()

    fun recondition(dafnyAST: DafnyAST): DafnyAST = DafnyAST(dafnyAST.topLevelElements.map(this::reconditionTopLevel))

    fun reconditionTopLevel(topLevelAST: TopLevelAST): TopLevelAST = when (topLevelAST) {
        is ClassAST -> reconditionClass(topLevelAST)
        is TraitAST -> reconditionTrait(topLevelAST)
        is MethodAST -> reconditionMethod(topLevelAST)
        is FunctionMethodAST -> reconditionFunctionMethod(topLevelAST)
        is MainFunctionAST -> reconditionMainFunction(topLevelAST)
        else -> throw UnsupportedOperationException()
    }

    fun reconditionClass(classAST: ClassAST): ClassAST {
        val reconditionedMethods = classAST.methods.map(this::reconditionMethod).toSet()
        val reconditionedFunctionMethods = classAST.functionMethods.map(this::reconditionFunctionMethod)

        return ClassAST(
            classAST.name,
            classAST.extends,
            emptySet(),
            reconditionedMethods union reconditionedFunctionMethods,
            classAST.fields,
            classAST.inheritedFields
        )
    }

    fun reconditionTrait(traitAST: TraitAST): TraitAST {
        val reconditionedMethodSignatures = traitAST.methods().map(this::reconditionMethodSignature).toSet()
        val reconditionedFunctionMethodSignatures =
            traitAST.functionMethods().map(this::reconditionFunctionMethodSignature).toSet()

        return TraitAST(
            traitAST.name,
            traitAST.extends(),
            emptySet(),
            reconditionedMethodSignatures union reconditionedFunctionMethodSignatures,
            traitAST.fields()
        )
    }

    fun reconditionMethod(methodAST: MethodAST): MethodAST {
        val reconditionedSignature = reconditionMethodSignature(methodAST.signature)
        val newStateDecl = AssignmentAST(newState, state)
        val reconditionedBody = reconditionSequence(methodAST.getBody())

        return MethodAST(
            reconditionedSignature,
            SequenceAST(listOf(newStateDecl) + reconditionedBody.statements)
        )
    }

    fun reconditionMethodSignature(signature: MethodSignatureAST): MethodSignatureAST =
        getReconditionedMethodSignature(signature)

    // convert to MethodAST then let reconditioning of method do the rest
    fun reconditionFunctionMethod(functionMethodAST: FunctionMethodAST): MethodAST = reconditionMethod(
        MethodAST(
            reconditionFunctionMethodSignature(functionMethodAST.signature),
            SequenceAST(
                listOf(
                    AssignmentAST(
                        IdentifierAST(FM_RETURNS, functionMethodAST.returnType()),
                        functionMethodAST.body
                    )
                )
            )
        )
    )

    fun reconditionFunctionMethodSignature(signature: FunctionMethodSignatureAST): MethodSignatureAST =
        getReconditionedFunctionMethodSignature(signature)

    fun reconditionMainFunction(mainFunctionAST: MainFunctionAST): MainFunctionAST {
        val stateDecl = DeclarationAST(newState, MapConstructorAST(STATE_TYPE.keyType, STATE_TYPE.valueType))
        val reconditionedBody = reconditionSequence(mainFunctionAST.sequenceAST)

        return MainFunctionAST(SequenceAST(listOf(stateDecl) + reconditionedBody.statements))
    }

    fun reconditionSequence(sequenceAST: SequenceAST): SequenceAST =
        SequenceAST(sequenceAST.statements.map(this::reconditionStatement).reduceRight { l, r -> l + r })

    private fun getReconditionedMethodSignature(signature: MethodSignatureAST): MethodSignatureAST {
        if (!methodSignatures.containsKey(signature.name)) {
            methodSignatures[signature.name] = MethodSignatureAST(
                signature.name,
                signature.params + additionalParams,
                signature.returns + additionalReturns
            )
        }

        return methodSignatures[signature.name]!!
    }

    private fun getReconditionedFunctionMethodSignature(signature: FunctionMethodSignatureAST): MethodSignatureAST {
        if (!methodSignatures.containsKey(signature.name)) {
            methodSignatures[signature.name] = MethodSignatureAST(
                signature.name,
                signature.params + additionalParams,
                listOf(IdentifierAST(FM_RETURNS, signature.returnType)) + additionalReturns
            )
        }

        return methodSignatures[signature.name]!!
    }

    /* ==================================== STATEMENTS ======================================== */

    fun reconditionStatement(statementAST: StatementAST): List<StatementAST> = when (statementAST) {
        is BreakAST -> listOf(statementAST)
        is MultiAssignmentAST -> reconditionMultiAssignment(statementAST)
        is TypedDeclarationAST -> reconditionTypedDeclaration(statementAST)
        is MultiDeclarationAST -> reconditionMultiDeclaration(statementAST)
        is IfStatementAST -> reconditionIfStatement(statementAST)
        is CounterLimitedWhileLoopAST -> reconditionCounterLimitedWhileLoop(statementAST)
        is WhileLoopAST -> reconditionWhileLoop(statementAST)
        is PrintAST -> reconditionPrint(statementAST)
        is VoidMethodCallAST -> reconditionVoidMethodCall(statementAST)
    }

    fun reconditionMultiAssignment(multiAssignmentAST: MultiAssignmentAST): List<StatementAST> {
        val (reconditionedIdentifiers, identifierDependents) = reconditionExpressionList(multiAssignmentAST.identifiers)
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(multiAssignmentAST.exprs)

        return identifierDependents + exprDependents +
                MultiAssignmentAST(reconditionedIdentifiers.map { it as IdentifierAST }, reconditionedExprs)
    }

    fun reconditionTypedDeclaration(typedDeclarationAST: TypedDeclarationAST): List<StatementAST> {
        val (reconditionedIdentifier, identifierDependents) = reconditionIdentifier(typedDeclarationAST.identifier)
        val (reconditionedExpr, exprDependents) = typedDeclarationAST.expr?.let(this::reconditionExpression)
            ?: Pair(null, emptyList())

        return identifierDependents + exprDependents + TypedDeclarationAST(reconditionedIdentifier, reconditionedExpr)
    }

    fun reconditionMultiDeclaration(multiDeclarationAST: MultiDeclarationAST): List<StatementAST> {
        val (reconditionedIdentifiers, identifierDependents) = reconditionExpressionList(multiDeclarationAST.identifiers)
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(multiDeclarationAST.exprs)

        return identifierDependents + exprDependents +
                MultiDeclarationAST(reconditionedIdentifiers.map { it as IdentifierAST }, reconditionedExprs)
    }

    fun reconditionIfStatement(ifStatementAST: IfStatementAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(ifStatementAST.condition)
        return dependents + IfStatementAST(
            newCondition,
            reconditionSequence(ifStatementAST.ifBranch),
            ifStatementAST.elseBranch?.let(this::reconditionSequence)
        )
    }

    fun reconditionCounterLimitedWhileLoop(whileLoopAST: CounterLimitedWhileLoopAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(whileLoopAST.condition)
        val reconditionedBody = reconditionSequence(whileLoopAST.body)

        return dependents + CounterLimitedWhileLoopAST(
            whileLoopAST.counterInitialisation,
            whileLoopAST.terminationCheck,
            whileLoopAST.counterUpdate,
            newCondition,
            SequenceAST(reconditionedBody.statements + dependents)
        )
    }

    fun reconditionWhileLoop(whileLoopAST: WhileLoopAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(whileLoopAST.condition)
        val reconditionedBody = reconditionSequence(whileLoopAST.body)

        return dependents + WhileLoopAST(newCondition, SequenceAST(reconditionedBody.statements + dependents))
    }

    fun reconditionPrint(printAST: PrintAST): List<StatementAST> {
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(printAST.expr)
        return exprDependents + PrintAST(reconditionedExprs)
    }

    fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST): List<StatementAST> {
        val method = getReconditionedMethodSignature(voidMethodCallAST.method)
        val (params, dependents) = reconditionExpressionList(voidMethodCallAST.params)

        return dependents + VoidMethodCallAST(method, params + listOf(newState, getHash(voidMethodCallAST)))
    }

    private fun reconditionExpressionList(exprs: List<ExpressionAST>): Pair<List<ExpressionAST>, List<StatementAST>> =
        exprs.map(this::reconditionExpression).fold(Pair(emptyList(), emptyList())) { acc, r ->
            Pair(acc.first + r.first, acc.second + r.second)
        }

    fun reconditionExpression(expressionAST: ExpressionAST): Pair<ExpressionAST, List<StatementAST>> = TODO()

    fun reconditionIdentifier(identifierAST: IdentifierAST): Pair<IdentifierAST, List<StatementAST>> = TODO()

    companion object {
        private const val FM_RETURNS = "r1"
        private const val ID_NAME = "id"
        private const val NEW_STATE_NAME = "newState"
        private const val STATE_NAME = "state"

        private val ID_TYPE = StringType
        private val STATE_TYPE = MapType(StringType, BoolType)

        private val id = IdentifierAST(ID_NAME, ID_TYPE)
        private val state = IdentifierAST(STATE_NAME, STATE_TYPE)
        private val newState = IdentifierAST(NEW_STATE_NAME, STATE_TYPE)

        private val additionalParams = listOf(state, id)
        private val additionalReturns = listOf(newState)

        private fun <T> getHash(obj: T) = StringLiteralAST(obj.hashCode().toString())
    }
}
