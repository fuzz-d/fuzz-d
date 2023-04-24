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
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAST
import fuzzd.generator.ast.ExpressionAST.MatchExpressionAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.MultisetIndexAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SequenceIndexAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.DataStructureMemberDeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.identifier_generator.NameGenerator.SafetyIdGenerator
import fuzzd.generator.ast.operators.BinaryOperator.MathematicalBinaryOperator
import fuzzd.logging.Logger
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_ARRAY_INDEX
import fuzzd.utils.safetyMap

class Reconditioner(private val logger: Logger, private val ids: Set<String>? = null) : ASTReconditioner {
    private val reconditionedClasses = mutableMapOf<ClassAST, ClassAST>()
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

        val reconditionedClass = ClassAST(
            classAST.name,
            classAST.extends,
            reconditionedFunctionMethods,
            reconditionedMethods,
            classAST.fields,
            classAST.inheritedFields,
        )

        reconditionedClasses[classAST] = reconditionedClass

        return reconditionedClass
    }

    override fun reconditionMethod(methodAST: MethodAST): MethodAST {
        methodAST.setBody(reconditionSequence(methodAST.getBody()))
        return methodAST
    }

    override fun reconditionMainFunction(mainFunction: MainFunctionAST) =
        MainFunctionAST(reconditionSequence(mainFunction.sequenceAST))

    override fun reconditionFunctionMethod(functionMethodAST: FunctionMethodAST): FunctionMethodAST =
        FunctionMethodAST(functionMethodAST.signature, reconditionExpression(functionMethodAST.body))

    override fun reconditionSequence(sequence: SequenceAST): SequenceAST =
        SequenceAST(sequence.statements.map(this::reconditionStatement))

    override fun reconditionStatement(statement: StatementAST) = when (statement) {
        is BreakAST -> statement
        is DataStructureMemberDeclarationAST -> reconditionDataStructureMemberDeclaration(statement)
        is MultiAssignmentAST -> reconditionMultiAssignmentAST(statement) // covers AssignmentAST
        is MultiTypedDeclarationAST -> reconditionMultiTypedDeclarationAST(statement)
        is MultiDeclarationAST -> reconditionMultiDeclarationAST(statement) // covers DeclarationAST
        is MatchStatementAST -> reconditionMatchStatement(statement)
        is IfStatementAST -> reconditionIfStatement(statement)
        is WhileLoopAST -> reconditionWhileLoopAST(statement)
        is PrintAST -> reconditionPrintAST(statement)
        is VoidMethodCallAST -> reconditionVoidMethodCall(statement)
    }

    override fun reconditionDataStructureMemberDeclaration(declarationAST: DataStructureMemberDeclarationAST) =
        DataStructureMemberDeclarationAST(
            reconditionIdentifier(declarationAST.identifier),
            reconditionIdentifier(declarationAST.dataStructure),
        )

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

    override fun reconditionMatchStatement(matchStatement: MatchStatementAST): MatchStatementAST = MatchStatementAST(
        reconditionExpression(matchStatement.match),
        matchStatement.cases.map { (case, seq) ->
            Pair(case, reconditionSequence(seq))
        },
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

    override fun reconditionPrintAST(printAST: PrintAST) = PrintAST(printAST.expr.map(this::reconditionExpression))

    override fun reconditionExpression(expression: ExpressionAST): ExpressionAST = when (expression) {
        is BinaryExpressionAST -> reconditionBinaryExpression(expression)
        is UnaryExpressionAST -> reconditionUnaryExpression(expression)
        is ModulusExpressionAST -> reconditionModulusExpression(expression)
        is MultisetConversionAST -> reconditionMultisetConversion(expression)
        is TernaryExpressionAST -> reconditionTernaryExpression(expression)
        is IdentifierAST -> reconditionIdentifier(expression)
        is IndexAST -> reconditionIndex(expression)
        is StringLiteralAST, is LiteralAST, is ArrayInitAST -> expression // don't need to do anything
        is ClassInstantiationAST -> reconditionClassInstantiation(expression)
        is ArrayLengthAST -> reconditionArrayLengthAST(expression)
        is NonVoidMethodCallAST -> reconditionNonVoidMethodCallAST(expression)
        is FunctionMethodCallAST -> reconditionFunctionMethodCall(expression)
        is SetDisplayAST -> reconditionSetDisplay(expression)
        is MapConstructorAST -> reconditionMapConstructor(expression)
        is SequenceDisplayAST -> reconditionSequenceDisplay(expression)
        is DatatypeDestructorAST -> reconditionDatatypeDestructor(expression)
        is DatatypeInstantiationAST -> reconditionDatatypeInstantiation(expression)
        is DatatypeUpdateAST -> reconditionDatatypeUpdate(expression)
        is MatchExpressionAST -> reconditionMatchExpression(expression)
    }

    override fun reconditionDatatypeDestructor(destructor: DatatypeDestructorAST): DatatypeDestructorAST =
        DatatypeDestructorAST(
            reconditionExpression(destructor.datatypeInstance),
            reconditionIdentifier(destructor.field),
        )

    override fun reconditionDatatypeInstantiation(instantiation: DatatypeInstantiationAST): DatatypeInstantiationAST =
        DatatypeInstantiationAST(
            instantiation.datatype,
            instantiation.constructor,
            instantiation.params.map(this::reconditionExpression),
        )

    override fun reconditionDatatypeUpdate(update: DatatypeUpdateAST): DatatypeUpdateAST =
        DatatypeUpdateAST(
            reconditionExpression(update.datatypeInstance),
            update.updates.map { (ident, expr) ->
                Pair(ident, reconditionExpression(expr))
            },
        )

    override fun reconditionMatchExpression(matchExpression: MatchExpressionAST): MatchExpressionAST =
        MatchExpressionAST(
            reconditionExpression(matchExpression.match),
            matchExpression.type,
            matchExpression.cases.map { (case, expr) ->
                Pair(case, reconditionExpression(expr))
            },
        )

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

    override fun reconditionModulusExpression(modulus: ModulusExpressionAST): ModulusExpressionAST =
        ModulusExpressionAST(reconditionExpression(modulus.expr))

    override fun reconditionMultisetConversion(multisetConversion: MultisetConversionAST): MultisetConversionAST =
        MultisetConversionAST(reconditionExpression(multisetConversion.expr))

    override fun reconditionFunctionMethodCall(functionMethodCall: FunctionMethodCallAST): ExpressionAST =
        FunctionMethodCallAST(
            functionMethodCall.function,
            functionMethodCall.params.map(this::reconditionExpression),
        )

    override fun reconditionIdentifier(identifierAST: IdentifierAST): IdentifierAST = when (identifierAST) {
        is ArrayIndexAST -> {
            val reconditionedArray = reconditionIdentifier(identifierAST.array)
            val reconditionedIndex = reconditionExpression(identifierAST.index)

            val safetyId = safetyIdGenerator.newValue()
            idsMap[safetyId] = identifierAST
            if (requiresSafety(safetyId)) {
                // log advanced reconditioning applications
                if (ids != null) {
                    logger.log { "$safetyId: Advanced reconditioning requires safety for array index $identifierAST" }
                }

                ArrayIndexAST(
                    reconditionedArray,
                    FunctionMethodCallAST(
                        SAFE_ARRAY_INDEX.signature,
                        listOf(reconditionedIndex, ArrayLengthAST(identifierAST.array)),
                    ),
                )
            } else {
                ArrayIndexAST(reconditionedArray, reconditionedIndex)
            }
        }

        is IndexAssignAST -> reconditionIndexAssign(identifierAST)

        is ClassInstanceFieldAST -> ClassInstanceFieldAST(
            reconditionIdentifier(identifierAST.classInstance),
            reconditionIdentifier(identifierAST.classField),
        )

        else -> identifierAST
    }

    override fun reconditionIndex(indexAST: IndexAST): ExpressionAST = when (indexAST) {
        is MapIndexAST -> MapIndexAST(reconditionExpression(indexAST.map), reconditionExpression(indexAST.key))
        is MultisetIndexAST -> MultisetIndexAST(
            reconditionExpression(indexAST.multiset),
            reconditionExpression(indexAST.key),
        )

        is SequenceIndexAST -> {
            val reconditionedSequence = reconditionExpression(indexAST.sequence)
            val reconditionedIndex = reconditionExpression(indexAST.key)

            val safetyId = safetyIdGenerator.newValue()
            idsMap[safetyId] = indexAST

            if (requiresSafety(safetyId)) {
                if (ids != null) {
                    logger.log { "$safetyId: Advanced reconditioning requires safety for array index $indexAST" }
                }

                SequenceIndexAST(
                    reconditionedSequence,
                    FunctionMethodCallAST(
                        SAFE_ARRAY_INDEX.signature,
                        listOf(reconditionedIndex, ModulusExpressionAST(reconditionedSequence)),
                    ),
                )
            } else {
                SequenceIndexAST(reconditionedSequence, reconditionedIndex)
            }
        }

        else -> throw UnsupportedOperationException()
    }

    private fun reconditionIndexAssign(indexAssign: IndexAssignAST): IndexAssignAST {
        val ident = reconditionIdentifier(indexAssign.ident)
        val key = reconditionExpression(indexAssign.key)
        val value = reconditionExpression(indexAssign.value)

        return when (indexAssign.type()) {
            is MapType -> IndexAssignAST(ident, key, value)
            is MultisetType -> {
                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = indexAssign

                if (requiresSafety(safetyId)) {
                    if (ids != null) {
                        logger.log { "$safetyId: Advanced reconditioning requires safety for multiset index assign $indexAssign" }
                    }

                    IndexAssignAST(ident, key, FunctionMethodCallAST(ABSOLUTE.signature, listOf(value)))
                } else {
                    IndexAssignAST(ident, key, value)
                }
            }

            is SequenceType -> {
                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = indexAssign

                if (requiresSafety(safetyId)) {
                    if (ids != null) {
                        logger.log { "$safetyId: Advanced reconditioning requires safety for seqeunce index assign $indexAssign" }
                    }

                    IndexAssignAST(
                        ident,
                        FunctionMethodCallAST(SAFE_ARRAY_INDEX.signature, listOf(key, ModulusExpressionAST(ident))),
                        value,
                    )
                } else {
                    IndexAssignAST(ident, key, value)
                }
            }

            else -> throw UnsupportedOperationException()
        }
    }

    override fun reconditionTernaryExpression(ternaryExpression: TernaryExpressionAST): ExpressionAST =
        TernaryExpressionAST(
            reconditionExpression(ternaryExpression.condition),
            reconditionExpression(ternaryExpression.ifBranch),
            reconditionExpression(ternaryExpression.elseBranch),
        )

    override fun reconditionClassInstantiation(classInstantiation: ClassInstantiationAST): ExpressionAST =
        ClassInstantiationAST(
            reconditionedClasses.getValue(classInstantiation.clazz),
            classInstantiation.params.map(this::reconditionExpression),
        )

    override fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST =
        ArrayLengthAST(reconditionIdentifier(arrayLengthAST.array))

    override fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST =
        NonVoidMethodCallAST(
            nonVoidMethodCall.method,
            nonVoidMethodCall.params.map(this::reconditionExpression),
        )

    override fun reconditionSetDisplay(setDisplayAST: SetDisplayAST): ExpressionAST = SetDisplayAST(
        setDisplayAST.exprs.map(this::reconditionExpression),
        setDisplayAST.isMultiset,
    )

    override fun reconditionMapConstructor(mapConstructorAST: MapConstructorAST): MapConstructorAST = MapConstructorAST(
        mapConstructorAST.keyType,
        mapConstructorAST.valueType,
        mapConstructorAST.assignments.map { (k, v) -> Pair(reconditionExpression(k), reconditionExpression(v)) },
    )

    override fun reconditionSequenceDisplay(sequenceDisplayAST: SequenceDisplayAST): SequenceDisplayAST =
        SequenceDisplayAST(sequenceDisplayAST.exprs.map(this::reconditionExpression))
}
