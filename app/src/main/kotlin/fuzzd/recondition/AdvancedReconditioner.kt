package fuzzd.recondition

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
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

    fun reconditionMethodSignature(signature: MethodSignatureAST): MethodSignatureAST = MethodSignatureAST(
        signature.name,
        signature.params + additionalParams,
        signature.returns + additionalReturns
    )

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
        MethodSignatureAST(
            signature.name,
            signature.params + additionalParams,
            listOf(IdentifierAST(FM_RETURNS, signature.returnType)) + additionalReturns
        )

    fun reconditionMainFunction(mainFunctionAST: MainFunctionAST): MainFunctionAST {
        val stateDecl = DeclarationAST(newState, MapConstructorAST(STATE_TYPE.keyType, STATE_TYPE.valueType))
        val reconditionedBody = reconditionSequence(mainFunctionAST.sequenceAST)

        return MainFunctionAST(SequenceAST(listOf(stateDecl) + reconditionedBody.statements))
    }

    fun reconditionSequence(sequenceAST: SequenceAST): SequenceAST =
        SequenceAST(sequenceAST.statements.map(this::reconditionStatement).reduceRight { l, r -> l + r })

    /* ==================================== STATEMENTS ======================================== */

    fun reconditionStatement(statementAST: StatementAST): List<StatementAST> = when (statementAST) {
        is BreakAST -> listOf(statementAST)
        is MultiAssignmentAST -> TODO()
        is TypedDeclarationAST -> TODO()
        is MultiDeclarationAST -> TODO()
        is IfStatementAST -> reconditionIfStatementAST(statementAST)
        is CounterLimitedWhileLoopAST -> reconditionCounterLimitedWhileLoopAST(statementAST)
        is WhileLoopAST -> reconditionWhileLoopAST(statementAST)
        is PrintAST -> TODO()
        is VoidMethodCallAST -> TODO()
    }

    fun reconditionMultiAssignmentAST(multiAssignmentAST: MultiAssignmentAST): List<StatementAST> = TODO()

    fun reconditionMultiDeclarationAST(multiDeclarationAST: MultiDeclarationAST): List<StatementAST> = TODO()

    fun reconditionIfStatementAST(ifStatementAST: IfStatementAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(ifStatementAST.condition)
        return dependents + IfStatementAST(
            newCondition,
            reconditionSequence(ifStatementAST.ifBranch),
            ifStatementAST.elseBranch?.let(this::reconditionSequence)
        )
    }

    fun reconditionCounterLimitedWhileLoopAST(whileLoopAST: CounterLimitedWhileLoopAST): List<StatementAST> {
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

    fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(whileLoopAST.condition)
        val reconditionedBody = reconditionSequence(whileLoopAST.body)

        return dependents + WhileLoopAST(newCondition, SequenceAST(reconditionedBody.statements + dependents))
    }

    fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST): List<StatementAST> = TODO()

    fun reconditionExpression(expressionAST: ExpressionAST): Pair<ExpressionAST, List<StatementAST>> = TODO()

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
    }
}
