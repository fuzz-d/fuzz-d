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
import fuzzd.generator.ast.StatementAST.DeclarationAST
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
        val stateDecl = DeclarationAST(state, MapConstructorAST(STATE_TYPE.keyType, STATE_TYPE.valueType))
        val reconditionedBody = reconditionSequence(mainFunctionAST.sequenceAST)

        return MainFunctionAST(SequenceAST(listOf(stateDecl) + reconditionedBody.statements))
    }

    fun reconditionSequence(sequenceAST: SequenceAST): SequenceAST =
        SequenceAST(sequenceAST.statements.map(this::reconditionStatement).reduceRight { l, r -> l + r })

    fun reconditionStatement(statementAST: StatementAST): List<StatementAST> = TODO()

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
