package fuzzd.recondition

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.ComprehensionInitialisedArrayInitAST
import fuzzd.generator.ast.ExpressionAST.DataStructureMapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.DataStructureSetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntRangeMapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.IntRangeSetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.MapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAST
import fuzzd.generator.ast.ExpressionAST.MatchExpressionAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.MultisetIndexAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.SequenceComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SequenceIndexAST
import fuzzd.generator.ast.ExpressionAST.SetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ValueInitialisedArrayInitAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssertStatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.ConjunctiveAssertStatement
import fuzzd.generator.ast.StatementAST.ForLoopAST
import fuzzd.generator.ast.StatementAST.ForallStatementAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VerificationAwareWhileLoopAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.DataStructureType.MapType
import fuzzd.generator.ast.Type.DataStructureType.MultisetType
import fuzzd.generator.ast.Type.DataStructureType.SequenceType
import fuzzd.generator.ast.identifier_generator.NameGenerator.SafetyIdGenerator
import fuzzd.generator.ast.operators.BinaryOperator.MathematicalBinaryOperator
import fuzzd.logging.Logger
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_INDEX
import fuzzd.utils.safetyMap

class Reconditioner(private val logger: Logger, private val ids: Set<String>? = null) : ASTReconditioner {
    private val reconditionedClasses = mutableMapOf<ClassAST, ClassAST>()
    private fun requiresSafety(str: String) = ids == null || str in ids

    private val safetyIdGenerator = SafetyIdGenerator()
    val idsMap = mutableMapOf<String, ASTElement>()

    override fun recondition(dafnyAST: DafnyAST): DafnyAST {
        // needs to recondition in the same order as Advanced Reconditioning
        val reconditionedDatatypes = dafnyAST.topLevelElements.filterIsInstance<DatatypeAST>()
        val reconditionedTraits = dafnyAST.topLevelElements.filterIsInstance<TraitAST>()
        val reconditionedClasses = dafnyAST.topLevelElements.filterIsInstance<ClassAST>().map(this::reconditionClass)
        val reconditionedMethods = dafnyAST.topLevelElements.filterIsInstance<MethodAST>().map(this::reconditionMethod)
        val reconditionedFunctionMethods =
            dafnyAST.topLevelElements.filterIsInstance<FunctionMethodAST>().map(this::reconditionFunctionMethod)
        val reconditionedMain =
            reconditionMainFunction(dafnyAST.topLevelElements.first { it is MainFunctionAST } as MainFunctionAST)

        return DafnyAST(
            reconditionedDatatypes +
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
        FunctionMethodAST(functionMethodAST.signature, reconditionExpression(functionMethodAST.getBody()))

    override fun reconditionSequence(sequence: SequenceAST): SequenceAST =
        SequenceAST(sequence.statements.map(this::reconditionStatement))

    override fun reconditionStatement(statement: StatementAST) = when (statement) {
        is ConjunctiveAssertStatement -> reconditionConjunctiveAssertStatement(statement)
        is AssertStatementAST -> reconditionAssertStatement(statement)
        is BreakAST -> statement
        is MultiAssignmentAST -> reconditionMultiAssignmentAST(statement) // covers AssignmentAST
        is MultiTypedDeclarationAST -> reconditionMultiTypedDeclarationAST(statement)
        is MultiDeclarationAST -> reconditionMultiDeclarationAST(statement) // covers DeclarationAST
        is MatchStatementAST -> reconditionMatchStatement(statement)
        is IfStatementAST -> reconditionIfStatement(statement)
        is ForLoopAST -> reconditionForLoopStatement(statement)
        is ForallStatementAST -> reconditionForallStatement(statement)
        is WhileLoopAST -> reconditionWhileLoopAST(statement)
        is PrintAST -> reconditionPrintAST(statement)
        is VoidMethodCallAST -> reconditionVoidMethodCall(statement)
        else -> throw UnsupportedOperationException()
    }

    override fun reconditionConjunctiveAssertStatement(assertStatement: ConjunctiveAssertStatement): ConjunctiveAssertStatement = ConjunctiveAssertStatement(
        reconditionExpression(assertStatement.baseExpr),
        assertStatement.exprs.map(this::reconditionExpression).toMutableSet(),
    )

    override fun reconditionAssertStatement(assertStatement: AssertStatementAST): AssertStatementAST = AssertStatementAST(reconditionExpression(assertStatement.expr))

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

    override fun reconditionForLoopStatement(forLoopAST: ForLoopAST): ForLoopAST = ForLoopAST(
        forLoopAST.identifier,
        reconditionExpression(forLoopAST.bottomRange),
        reconditionExpression(forLoopAST.topRange),
        reconditionSequence(forLoopAST.body),
    )

    override fun reconditionForallStatement(forallStatementAST: ForallStatementAST): ForallStatementAST {
        val arrayIndex = forallStatementAST.assignment.identifier as ArrayIndexAST
        return ForallStatementAST(
            forallStatementAST.identifier,
            reconditionExpression(forallStatementAST.bottomRange),
            reconditionExpression(forallStatementAST.topRange),
            AssignmentAST(
                ArrayIndexAST(reconditionIdentifier(arrayIndex.array), arrayIndex.index),
                reconditionExpression(forallStatementAST.assignment.expr),
            ),
        )
    }

    override fun reconditionWhileLoopAST(whileLoopAST: WhileLoopAST) = when (whileLoopAST) {
        is VerificationAwareWhileLoopAST -> VerificationAwareWhileLoopAST(
            whileLoopAST.counter,
            whileLoopAST.modset,
            whileLoopAST.counterInitialisation,
            whileLoopAST.terminationCheck,
            whileLoopAST.counterUpdate,
            reconditionExpression(whileLoopAST.condition),
            whileLoopAST.decreases,
            whileLoopAST.invariants,
            reconditionSequence(whileLoopAST.body),
        )

        is CounterLimitedWhileLoopAST -> CounterLimitedWhileLoopAST(
            whileLoopAST.counterInitialisation,
            whileLoopAST.terminationCheck,
            whileLoopAST.counterUpdate,
            reconditionExpression(whileLoopAST.condition),
            whileLoopAST.annotations,
            reconditionSequence(whileLoopAST.body),
        )

        else -> WhileLoopAST(
            reconditionExpression(whileLoopAST.condition),
            whileLoopAST.annotations,
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
        is IndexAssignAST -> reconditionIndexAssign(expression)
        is ArrayInitAST -> reconditionArrayInit(expression)
        is StringLiteralAST, is LiteralAST -> expression // don't need to do anything
        is ClassInstantiationAST -> reconditionClassInstantiation(expression)
        is ArrayLengthAST -> reconditionArrayLengthAST(expression)
        is NonVoidMethodCallAST -> reconditionNonVoidMethodCallAST(expression)
        is FunctionMethodCallAST -> reconditionFunctionMethodCall(expression)
        is SetDisplayAST -> reconditionSetDisplay(expression)
        is SetComprehensionAST -> reconditionSetComprehension(expression)
        is MapConstructorAST -> reconditionMapConstructor(expression)
        is MapComprehensionAST -> reconditionMapComprehension(expression)
        is SequenceDisplayAST -> reconditionSequenceDisplay(expression)
        is SequenceComprehensionAST -> reconditionSequenceComprehension(expression)
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
                        SAFE_INDEX.signature,
                        listOf(reconditionedIndex, ArrayLengthAST(reconditionedArray)),
                    ),
                )
            } else {
                ArrayIndexAST(reconditionedArray, reconditionedIndex)
            }
        }

        is ClassInstanceFieldAST -> ClassInstanceFieldAST(
            reconditionIdentifier(identifierAST.classInstance),
            reconditionIdentifier(identifierAST.classField),
        )

        is DatatypeDestructorAST -> reconditionDatatypeDestructor(identifierAST)

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
                        SAFE_INDEX.signature,
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
        val ident = reconditionExpression(indexAssign.expression)
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
                        FunctionMethodCallAST(SAFE_INDEX.signature, listOf(key, ModulusExpressionAST(ident))),
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

    override fun reconditionArrayInit(arrayInit: ArrayInitAST): ArrayInitAST = when (arrayInit) {
        is ComprehensionInitialisedArrayInitAST -> reconditionComprehensionInitialisedArrayInit(arrayInit)
        is ValueInitialisedArrayInitAST -> reconditionValueInitialisedArrayInit(arrayInit)
        else -> arrayInit
    }

    private fun reconditionComprehensionInitialisedArrayInit(arrayInit: ComprehensionInitialisedArrayInitAST): ComprehensionInitialisedArrayInitAST =
        ComprehensionInitialisedArrayInitAST(
            arrayInit.length,
            arrayInit.identifier,
            reconditionExpression(arrayInit.expr),
        )

    private fun reconditionValueInitialisedArrayInit(arrayInit: ValueInitialisedArrayInitAST): ValueInitialisedArrayInitAST =
        ValueInitialisedArrayInitAST(
            arrayInit.length,
            arrayInit.values.map(this::reconditionExpression),
        )

    override fun reconditionArrayLengthAST(arrayLengthAST: ArrayLengthAST): ExpressionAST =
        ArrayLengthAST(reconditionIdentifier(arrayLengthAST.array))

    override fun reconditionNonVoidMethodCallAST(nonVoidMethodCall: NonVoidMethodCallAST): ExpressionAST =
        NonVoidMethodCallAST(
            nonVoidMethodCall.method,
            nonVoidMethodCall.params.map(this::reconditionExpression),
        )

    override fun reconditionSetDisplay(setDisplay: SetDisplayAST): ExpressionAST = SetDisplayAST(
        setDisplay.innerType,
        setDisplay.exprs.map(this::reconditionExpression),
        setDisplay.isMultiset,
    )

    override fun reconditionSetComprehension(setComprehension: SetComprehensionAST): SetComprehensionAST =
        when (setComprehension) {
            is IntRangeSetComprehensionAST -> reconditionIntRangeSetComprehension(setComprehension)
            is DataStructureSetComprehensionAST -> reconditionDataStructureSetComprehension(setComprehension)
            else -> throw UnsupportedOperationException()
        }

    private fun reconditionIntRangeSetComprehension(setComprehension: IntRangeSetComprehensionAST): IntRangeSetComprehensionAST =
        IntRangeSetComprehensionAST(
            setComprehension.identifier,
            reconditionExpression(setComprehension.bottomRange),
            reconditionExpression(setComprehension.topRange),
            reconditionExpression(setComprehension.expr),
        )

    private fun reconditionDataStructureSetComprehension(setComprehension: DataStructureSetComprehensionAST): DataStructureSetComprehensionAST =
        DataStructureSetComprehensionAST(
            setComprehension.identifier,
            reconditionExpression(setComprehension.dataStructure),
            reconditionExpression(setComprehension.expr),
        )

    override fun reconditionMapConstructor(mapConstructor: MapConstructorAST): MapConstructorAST = MapConstructorAST(
        mapConstructor.keyType,
        mapConstructor.valueType,
        mapConstructor.assignments.map { (k, v) -> Pair(reconditionExpression(k), reconditionExpression(v)) },
    )

    override fun reconditionMapComprehension(mapComprehension: MapComprehensionAST): MapComprehensionAST =
        when (mapComprehension) {
            is IntRangeMapComprehensionAST -> reconditionIntRangeMapComprehension(mapComprehension)
            is DataStructureMapComprehensionAST -> reconditionDataStructureMapComprehension(mapComprehension)
            else -> throw UnsupportedOperationException()
        }

    private fun reconditionIntRangeMapComprehension(mapComprehension: IntRangeMapComprehensionAST): IntRangeMapComprehensionAST =
        IntRangeMapComprehensionAST(
            mapComprehension.identifier,
            reconditionExpression(mapComprehension.bottomRange),
            reconditionExpression(mapComprehension.topRange),
            Pair(reconditionExpression(mapComprehension.assign.first), reconditionExpression(mapComprehension.assign.second)),
        )

    private fun reconditionDataStructureMapComprehension(mapComprehension: DataStructureMapComprehensionAST): DataStructureMapComprehensionAST =
        DataStructureMapComprehensionAST(
            mapComprehension.identifier,
            reconditionExpression(mapComprehension.dataStructure),
            Pair(reconditionExpression(mapComprehension.assign.first), reconditionExpression(mapComprehension.assign.second)),
        )

    override fun reconditionSequenceDisplay(sequenceDisplay: SequenceDisplayAST): SequenceDisplayAST =
        SequenceDisplayAST(sequenceDisplay.exprs.map(this::reconditionExpression))

    override fun reconditionSequenceComprehension(sequenceComprehension: SequenceComprehensionAST): SequenceComprehensionAST {
        val safetyId = safetyIdGenerator.newValue()
        idsMap[safetyId] = sequenceComprehension

        return if (requiresSafety(safetyId)) {
            if (ids != null) {
                logger.log { "$safetyId: Advanced reconditioning requires safety for sequence comprehension size ${sequenceComprehension.size}" }
            }

            SequenceComprehensionAST(
                FunctionMethodCallAST(ABSOLUTE.signature, listOf(sequenceComprehension.size)),
                sequenceComprehension.identifier,
                sequenceComprehension.annotations,
                reconditionExpression(sequenceComprehension.expr),
            )
        } else {
            SequenceComprehensionAST(
                sequenceComprehension.size,
                sequenceComprehension.identifier,
                sequenceComprehension.annotations,
                reconditionExpression(sequenceComprehension.expr),
            )
        }
    }
}
