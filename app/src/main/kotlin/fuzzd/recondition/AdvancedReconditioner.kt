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
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
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
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.identifier_generator.NameGenerator.TemporaryNameGenerator

class AdvancedReconditioner {
    private val classes = mutableMapOf<String, ClassAST>()
    private val traits = mutableMapOf<String, TraitAST>()
    private val methodSignatures = mutableMapOf<String, MethodSignatureAST>()
    private val tempGenerator = TemporaryNameGenerator()

    fun recondition(dafnyAST: DafnyAST): DafnyAST {
        // get top level view of program
        val reconditionedTraits = dafnyAST.topLevelElements.filterIsInstance<TraitAST>().map(this::reconditionTrait)
        val methods = dafnyAST.topLevelElements.filterIsInstance<MethodAST>()
        val functionMethods = dafnyAST.topLevelElements.filterIsInstance<FunctionMethodAST>()

        methods.forEach { reconditionMethodSignature(it.signature) }
        functionMethods.forEach { reconditionFunctionMethodSignature(it.signature) }

        val reconditionedClasses = dafnyAST.topLevelElements.filterIsInstance<ClassAST>().map(this::reconditionClass)
        val reconditionedMethods =
            methods.map(this::reconditionMethod) + functionMethods.map(this::reconditionFunctionMethod)

        return DafnyAST(reconditionedTraits + reconditionedClasses + reconditionedMethods)
    }

    fun reconditionClass(classAST: ClassAST): ClassAST {
        classAST.methods.forEach { reconditionMethodSignature(it.signature) }
        classAST.functionMethods.forEach { reconditionFunctionMethodSignature(it.signature) }

        val reconditionedMethods = classAST.methods.map(this::reconditionMethod).toSet()
        val reconditionedFunctionMethods = classAST.functionMethods.map(this::reconditionFunctionMethod).toSet()

        val clazz = ClassAST(
            classAST.name,
            classAST.extends.map { traits.getValue(it.name) }.toSet(),
            emptySet(),
            reconditionedMethods union reconditionedFunctionMethods,
            classAST.fields,
            classAST.inheritedFields,
        )

        classes[classAST.name] = clazz
        return clazz
    }

    fun reconditionTrait(traitAST: TraitAST): TraitAST {
        val reconditionedMethodSignatures = traitAST.methods().map(this::reconditionMethodSignature).toSet()
        val reconditionedFunctionMethodSignatures =
            traitAST.functionMethods().map(this::reconditionFunctionMethodSignature).toSet()

        val trait = TraitAST(
            traitAST.name,
            traitAST.extends().map { traits.getValue(it.name) }.toSet(),
            emptySet(),
            reconditionedMethodSignatures union reconditionedFunctionMethodSignatures,
            traitAST.fields(),
        )

        traits[traitAST.name] = trait
        return trait
    }

    fun reconditionMethod(methodAST: MethodAST): MethodAST {
        val reconditionedSignature = reconditionMethodSignature(methodAST.signature)
        val newStateDecl = AssignmentAST(newState, state)
        val reconditionedBody = reconditionSequence(methodAST.getBody())

        return MethodAST(
            reconditionedSignature,
            SequenceAST(listOf(newStateDecl) + reconditionedBody.statements),
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
                        functionMethodAST.body,
                    ),
                ),
            ),
        ),
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
        if (methodSignatures.containsKey(signature.name)) {
            methodSignatures[signature.name] = MethodSignatureAST(
                signature.name,
                signature.params + additionalParams,
                signature.returns + additionalReturns,
            )
        }

        return methodSignatures.getValue(signature.name)
    }

    private fun getReconditionedFunctionMethodSignature(signature: FunctionMethodSignatureAST): MethodSignatureAST {
        if (!methodSignatures.containsKey(signature.name)) {
            methodSignatures[signature.name] = MethodSignatureAST(
                signature.name,
                signature.params + additionalParams,
                listOf(IdentifierAST(FM_RETURNS, signature.returnType)) + additionalReturns,
            )
        }

        return methodSignatures.getValue(signature.name)
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
            ifStatementAST.elseBranch?.let(this::reconditionSequence),
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
            SequenceAST(reconditionedBody.statements + dependents),
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

    /* =========================================== EXPRESSIONS =========================================== */

    fun reconditionExpression(expressionAST: ExpressionAST): Pair<ExpressionAST, List<StatementAST>> =
        when (expressionAST) {
            is BinaryExpressionAST -> reconditionBinaryExpression(expressionAST)
            is UnaryExpressionAST -> reconditionUnaryExpression(expressionAST)
            is TernaryExpressionAST -> reconditionTernaryExpression(expressionAST)
            is IdentifierAST -> reconditionIdentifier(expressionAST)
            is LiteralAST, is ArrayInitAST -> Pair(expressionAST, emptyList()) // do nothing
            is ClassInstantiationAST -> reconditionClassInstantiation(expressionAST)
            is ArrayLengthAST -> reconditionArrayLength(expressionAST)
            is NonVoidMethodCallAST -> reconditionNonVoidMethodCall(expressionAST)
            is FunctionMethodCallAST -> reconditionFunctionMethodCall(expressionAST)
            else -> throw UnsupportedOperationException()
        }

    fun reconditionBinaryExpression(binaryExpressionAST: BinaryExpressionAST): Pair<ExpressionAST, List<StatementAST>> =
        TODO()

    fun reconditionUnaryExpression(unaryExpressionAST: UnaryExpressionAST): Pair<UnaryExpressionAST, List<StatementAST>> {
        val (reconditionedExpression, dependents) = reconditionExpression(unaryExpressionAST.expr)

        return Pair(UnaryExpressionAST(reconditionedExpression, unaryExpressionAST.operator), dependents)
    }

    fun reconditionTernaryExpression(ternaryExpressionAST: TernaryExpressionAST): Pair<TernaryExpressionAST, List<StatementAST>> {
        val (newCondition, conditionDependents) = reconditionExpression(ternaryExpressionAST.condition)
        val (newIfBranch, ifBranchDependents) = reconditionExpression(ternaryExpressionAST.ifBranch)
        val (newElseBranch, elseBranchDependents) = reconditionExpression(ternaryExpressionAST.elseBranch)

        return Pair(
            TernaryExpressionAST(newCondition, newIfBranch, newElseBranch),
            conditionDependents + ifBranchDependents + elseBranchDependents,
        )
    }

    fun reconditionClassInstantiation(classInstantiationAST: ClassInstantiationAST): Pair<ClassInstantiationAST, List<StatementAST>> {
        val reconditionedClass = classes.getValue(classInstantiationAST.clazz.name)
        val (params, dependents) = reconditionExpressionList(classInstantiationAST.params)

        return Pair(ClassInstantiationAST(reconditionedClass, params), dependents)
    }

    fun reconditionArrayLength(arrayLengthAST: ArrayLengthAST): Pair<ArrayLengthAST, List<StatementAST>> {
        val (identifier, dependents) = reconditionIdentifier(arrayLengthAST.array)
        return Pair(ArrayLengthAST(identifier), dependents)
    }

    fun reconditionIdentifier(identifierAST: IdentifierAST): Pair<IdentifierAST, List<StatementAST>> = TODO()

    fun reconditionNonVoidMethodCall(nonVoidMethodCallAST: NonVoidMethodCallAST): Pair<NonVoidMethodCallAST, List<StatementAST>> {
        TODO()
    }

    fun reconditionFunctionMethodCall(functionMethodCallAST: FunctionMethodCallAST): Pair<ExpressionAST, List<StatementAST>> {
        TODO()
    }

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
