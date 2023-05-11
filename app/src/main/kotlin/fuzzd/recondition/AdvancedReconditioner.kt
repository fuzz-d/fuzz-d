package fuzzd.recondition

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ClassInstanceFunctionMethodSignatureAST
import fuzzd.generator.ast.ClassInstanceMethodSignatureAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.DatatypeConstructorAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.ComprehensionInitialisedArrayInitAST
import fuzzd.generator.ast.ExpressionAST.DataStructureMapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.DataStructureSetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstanceAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntRangeMapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.IntRangeSetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.MapComprehensionAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.MapIndexAST
import fuzzd.generator.ast.ExpressionAST.MatchExpressionAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.MultisetIndexAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.ObjectOrientedInstanceAST
import fuzzd.generator.ast.ExpressionAST.SequenceComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SequenceDisplayAST
import fuzzd.generator.ast.ExpressionAST.SequenceIndexAST
import fuzzd.generator.ast.ExpressionAST.SetComprehensionAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.TopLevelDatatypeInstanceAST
import fuzzd.generator.ast.ExpressionAST.TraitInstanceAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.ValueInitialisedArrayInitAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssertStatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.DataStructureAssignSuchThatStatement
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.ForLoopAST
import fuzzd.generator.ast.StatementAST.ForallStatementAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiAssignmentAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.DataStructureType
import fuzzd.generator.ast.Type.DataStructureType.MapType
import fuzzd.generator.ast.Type.DataStructureType.MultisetType
import fuzzd.generator.ast.Type.DataStructureType.SequenceType
import fuzzd.generator.ast.Type.DataStructureType.SetType
import fuzzd.generator.ast.Type.DataStructureType.StringType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.TopLevelDatatypeType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.VerifierAnnotationAST.DecreasesAnnotation
import fuzzd.generator.ast.VerifierAnnotationAST.ModifiesAnnotation
import fuzzd.generator.ast.VerifierAnnotationAST.ReadsAnnotation
import fuzzd.generator.ast.identifier_generator.NameGenerator.SafetyIdGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.TemporaryNameGenerator
import fuzzd.generator.ast.operators.BinaryOperator.DifferenceOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.UnionOperator
import fuzzd.utils.ADVANCED_ABSOLUTE
import fuzzd.utils.ADVANCED_RECONDITION_CLASS
import fuzzd.utils.ADVANCED_SAFE_ARRAY_INDEX
import fuzzd.utils.ADVANCED_SAFE_DIV_INT
import fuzzd.utils.ADVANCED_SAFE_MODULO_INT
import fuzzd.utils.foldPair

class AdvancedReconditioner {
    private val classes = mutableMapOf<String, ClassAST>()
    private val traits = mutableMapOf<String, TraitAST>()
    private val datatypes = mutableMapOf<String, DatatypeAST>()

    private val methodSignatures = mutableMapOf<String, MethodSignatureAST>()
    private val tempGenerator = TemporaryNameGenerator()

    private val safetyIdGenerator = SafetyIdGenerator()

    val idsMap = mutableMapOf<String, ASTElement>()

    fun recondition(dafnyAST: DafnyAST): DafnyAST {
        // get top level view of program
        val methods = dafnyAST.topLevelElements.filterIsInstance<MethodAST>()
        val functionMethods = dafnyAST.topLevelElements.filterIsInstance<FunctionMethodAST>()

        methods.forEach { reconditionMethodSignature(it.signature) }
        functionMethods.forEach { reconditionFunctionMethodSignature(it.signature) }

        val reconditionedClasses = dafnyAST.topLevelElements.filterIsInstance<ClassAST>().map(this::reconditionClass)
        val reconditionedTraits = dafnyAST.topLevelElements.filterIsInstance<TraitAST>().map(this::reconditionTrait)
        val reconditionedDatatypes = dafnyAST.topLevelElements.filterIsInstance<DatatypeAST>()

        val reconditionedMethods =
            methods.map(this::reconditionMethod) + functionMethods.map(this::reconditionFunctionMethod)

        val reconditionedMain =
            reconditionMainFunction(dafnyAST.topLevelElements.first { it is MainFunctionAST } as MainFunctionAST)

        return DafnyAST(
            reconditionedDatatypes +
                reconditionedTraits +
                reconditionedClasses +
                reconditionedMethods +
                reconditionedMain,
        )
    }

    fun reconditionClass(classAST: ClassAST): ClassAST {
        if (classAST.name in classes) return classes.getValue(classAST.name)

        classAST.methods.forEach { reconditionMethodSignature(it.signature) }
        classAST.functionMethods.forEach { reconditionFunctionMethodSignature(it.signature) }

        val reconditionedMethods = classAST.methods.map(this::reconditionMethod).toSet()
        val reconditionedFunctionMethods = classAST.functionMethods.map(this::reconditionFunctionMethod).toSet()

        val clazz = ClassAST(
            classAST.name,
            classAST.extends.map { reconditionTrait(it) }.toSet(),
            emptySet(),
            reconditionedMethods union reconditionedFunctionMethods,
            classAST.fields.map { reconditionIdentifier(it).first }.toSet(),
            classAST.inheritedFields.map { reconditionIdentifier(it).first }.toSet(),
        )

        classes[classAST.name] = clazz
        return clazz
    }

    fun reconditionTrait(traitAST: TraitAST): TraitAST {
        if (traitAST.name in traits) return traits.getValue(traitAST.name)

        val reconditionedMethodSignatures = traitAST.methods().map(this::reconditionMethodSignature).toSet()
        val reconditionedFunctionMethodSignatures =
            traitAST.functionMethods().map(this::reconditionFunctionMethodSignature).toSet()

        val trait = TraitAST(
            traitAST.name,
            traitAST.extends().map { reconditionTrait(it) }.toSet(),
            emptySet(),
            reconditionedMethodSignatures union reconditionedFunctionMethodSignatures,
            traitAST.fields.map { reconditionIdentifier(it).first }.toSet(),
        )

        traits[traitAST.name] = trait
        return trait
    }

    fun reconditionDatatype(datatypeAST: DatatypeAST): DatatypeAST =
        if (datatypes.containsKey(datatypeAST.name)) {
            datatypes[datatypeAST.name]!!
        } else {
            val inductiveConstructors = datatypeAST.constructors.filter { it.fields.any { f -> f.type() == TopLevelDatatypeType(datatypeAST) } }
            val constructors = datatypeAST.constructors.filter { it !in inductiveConstructors }.map(this::reconditionDatatypeConstructor).toMutableList()
            val datatype = DatatypeAST(datatypeAST.name, constructors)
            datatype.constructors.addAll(
                inductiveConstructors.map { c ->
                    DatatypeConstructorAST(
                        c.name,
                        c.fields.map { TopLevelDatatypeInstanceAST(it.name, TopLevelDatatypeType(datatype), it.mutable, it.initialised()) },
                    )
                },
            )
            datatypes[datatypeAST.name] = datatype
            datatype
        }

    fun reconditionDatatypeConstructor(datatypeConstructorAST: DatatypeConstructorAST): DatatypeConstructorAST = DatatypeConstructorAST(
        datatypeConstructorAST.name,
        datatypeConstructorAST.fields.map { reconditionIdentifier(it).first }.toMutableList(),
    )

    fun reconditionMethod(methodAST: MethodAST): MethodAST {
        val reconditionedSignature = reconditionMethodSignature(methodAST.signature)
        val reconditionedBody = reconditionSequence(methodAST.getBody())

        return MethodAST(
            reconditionedSignature,
            SequenceAST(reconditionedBody.statements),
        )
    }

    fun reconditionMethodSignature(signature: MethodSignatureAST): MethodSignatureAST =
        getReconditionedMethodSignature(signature)

    // convert to MethodAST then let reconditioning of method do the rest
    fun reconditionFunctionMethod(functionMethodAST: FunctionMethodAST): MethodAST {
        val returnIdentifier = when (val returnType = functionMethodAST.returnType()) {
            is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(FM_RETURNS, returnType)
            else -> IdentifierAST(FM_RETURNS, returnType)
        }

        return reconditionMethod(
            MethodAST(
                reconditionFunctionMethodSignature(functionMethodAST.signature),
                SequenceAST(listOf(AssignmentAST(returnIdentifier, functionMethodAST.body))),
            ),
        )
    }

    fun reconditionFunctionMethodSignature(signature: FunctionMethodSignatureAST): MethodSignatureAST =
        getReconditionedFunctionMethodSignature(signature)

    fun reconditionMainFunction(mainFunctionAST: MainFunctionAST): MainFunctionAST {
        val stateDecl = DeclarationAST(
            state,
            ClassInstantiationAST(
                ADVANCED_RECONDITION_CLASS,
                listOf(
                    MapConstructorAST(
                        STATE_MAP_TYPE.keyType,
                        STATE_MAP_TYPE.valueType,
                    ),
                ),
            ),
        )
        val reconditionedBody = reconditionSequence(mainFunctionAST.sequenceAST)

        return MainFunctionAST(SequenceAST(listOf(stateDecl) + reconditionedBody.statements))
    }

    fun reconditionSequence(sequenceAST: SequenceAST): SequenceAST =
        SequenceAST(sequenceAST.statements.map(this::reconditionStatement).reduceRight { l, r -> l + r })

    private fun getReconditionedMethodSignature(signature: MethodSignatureAST): MethodSignatureAST {
        if (!methodSignatures.containsKey(signature.name)) {
            val annotations = signature.annotations + additionalParams.map { ModifiesAnnotation(it) }
            methodSignatures[signature.name] =
                MethodSignatureAST(
                    signature.name,
                    (signature.params + additionalParams).map { reconditionIdentifier(it).first },
                    signature.returns.map { reconditionIdentifier(it).first },
                    annotations.toMutableList(),
                )
        }

        return methodSignatures.getValue(signature.name)
    }

    private fun getReconditionedFunctionMethodSignature(signature: FunctionMethodSignatureAST): MethodSignatureAST {
        if (!methodSignatures.containsKey(signature.name)) {
            val returns = when (signature.returnType) {
                is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(FM_RETURNS, signature.returnType)
                else -> IdentifierAST(FM_RETURNS, signature.returnType)
            }

            val annotations = signature.annotations.map {
                if (it is ReadsAnnotation) ModifiesAnnotation(it.identifier) else it
            } + additionalParams.map { ModifiesAnnotation(it) }

            methodSignatures[signature.name] = MethodSignatureAST(
                signature.name,
                (signature.params + additionalParams).map { reconditionIdentifier(it).first },
                listOf(returns),
                annotations.toMutableList(),
            )
        }

        return methodSignatures.getValue(signature.name)
    }

    /* ==================================== STATEMENTS ======================================== */

    fun reconditionStatement(statementAST: StatementAST): List<StatementAST> = when (statementAST) {
        is AssertStatementAST -> reconditionAssertStatement(statementAST)
        is BreakAST -> listOf(statementAST)
        is MultiAssignmentAST -> reconditionMultiAssignment(statementAST)
        is MultiTypedDeclarationAST -> reconditionMultiTypedDeclaration(statementAST)
        is MultiDeclarationAST -> reconditionMultiDeclaration(statementAST)
        is MatchStatementAST -> reconditionMatchStatement(statementAST)
        is IfStatementAST -> reconditionIfStatement(statementAST)
        is ForLoopAST -> reconditionForLoopStatement(statementAST)
        is ForallStatementAST -> reconditionForallStatement(statementAST)
        is CounterLimitedWhileLoopAST -> reconditionCounterLimitedWhileLoop(statementAST)
        is WhileLoopAST -> reconditionWhileLoop(statementAST)
        is PrintAST -> reconditionPrint(statementAST)
        is VoidMethodCallAST -> reconditionVoidMethodCall(statementAST)
        else -> throw UnsupportedOperationException()
    }

    fun reconditionAssertStatement(assertStatementAST: AssertStatementAST): List<StatementAST> {
        val (reconditionedExpr, exprDependents) = reconditionExpression(assertStatementAST.expr)
        return exprDependents + AssertStatementAST(reconditionedExpr)
    }

    fun reconditionMultiAssignment(multiAssignmentAST: MultiAssignmentAST): List<StatementAST> {
        val (reconditionedIdentifiers, identifierDependents) = reconditionExpressionList(multiAssignmentAST.identifiers)
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(multiAssignmentAST.exprs)

        return identifierDependents + exprDependents +
            MultiAssignmentAST(reconditionedIdentifiers.map { it as IdentifierAST }, reconditionedExprs)
    }

    fun reconditionMultiTypedDeclaration(multiTypedDeclarationAST: MultiTypedDeclarationAST): List<StatementAST> {
        val (reconditionedIdentifiers, identifierDependents) = reconditionExpressionList(multiTypedDeclarationAST.identifiers)
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(multiTypedDeclarationAST.exprs)

        return identifierDependents + exprDependents + MultiTypedDeclarationAST(
            reconditionedIdentifiers.map { it as IdentifierAST },
            reconditionedExprs,
        )
    }

    fun reconditionMultiDeclaration(multiDeclarationAST: MultiDeclarationAST): List<StatementAST> {
        val (reconditionedIdentifiers, identifierDependents) = reconditionExpressionList(multiDeclarationAST.identifiers)
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(multiDeclarationAST.exprs)

        return identifierDependents + exprDependents +
            MultiDeclarationAST(reconditionedIdentifiers.map { it as IdentifierAST }, reconditionedExprs)
    }

    fun reconditionMatchStatement(matchStatementAST: MatchStatementAST): List<StatementAST> {
        val (match, matchDependents) = reconditionExpression(matchStatementAST.match)
        val cases = matchStatementAST.cases.map { (case, seq) ->
            val rseq = reconditionSequence(seq)
            Pair(case, rseq)
        }

        return matchDependents + MatchStatementAST(match, cases)
    }

    fun reconditionIfStatement(ifStatementAST: IfStatementAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(ifStatementAST.condition)
        return dependents + IfStatementAST(
            newCondition,
            reconditionSequence(ifStatementAST.ifBranch),
            ifStatementAST.elseBranch?.let(this::reconditionSequence),
        )
    }

    fun reconditionForLoopStatement(forLoopAST: ForLoopAST): List<StatementAST> {
        val (bottomRange, bottomRangeDependents) = reconditionExpression(forLoopAST.bottomRange)
        val (topRange, topRangeDependents) = reconditionExpression(forLoopAST.topRange)

        return bottomRangeDependents + topRangeDependents + ForLoopAST(forLoopAST.identifier, bottomRange, topRange, reconditionSequence(forLoopAST.body))
    }

    fun reconditionForallStatement(forallStatementAST: ForallStatementAST): List<StatementAST> {
        val (bottomRange, bottomRangeDependents) = reconditionExpression(forallStatementAST.bottomRange)
        val (topRange, topRangeDependents) = reconditionExpression(forallStatementAST.topRange)

        val arrayIndex = forallStatementAST.assignment.identifier as ArrayIndexAST
        val (array, arrayDependents) = reconditionIdentifier(arrayIndex.array)
        val (assignExpr, assignExprDependents) = reconditionExpression(forallStatementAST.assignment.expr)

        // conversion to equivalent for-loop due to possible method calls (not allowed in forall statement)
        return bottomRangeDependents + topRangeDependents + arrayDependents +
            ForLoopAST(
                forallStatementAST.identifier,
                bottomRange,
                topRange,
                SequenceAST(assignExprDependents + AssignmentAST(ArrayIndexAST(array, arrayIndex.index), assignExpr)),
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
            whileLoopAST.annotations,
            SequenceAST(reconditionedBody.statements + dependents),
        )
    }

    fun reconditionWhileLoop(whileLoopAST: WhileLoopAST): List<StatementAST> {
        val (newCondition, dependents) = reconditionExpression(whileLoopAST.condition)
        val reconditionedBody = reconditionSequence(whileLoopAST.body)

        return dependents + WhileLoopAST(newCondition, whileLoopAST.annotations, SequenceAST(reconditionedBody.statements + dependents))
    }

    fun reconditionPrint(printAST: PrintAST): List<StatementAST> {
        val (reconditionedExprs, exprDependents) = reconditionExpressionList(printAST.expr)
        return exprDependents + PrintAST(reconditionedExprs)
    }

    fun reconditionVoidMethodCall(voidMethodCallAST: VoidMethodCallAST): List<StatementAST> =
        if (voidMethodCallAST.method is ClassInstanceMethodSignatureAST) {
            val (instance, instanceDeps) = reconditionIdentifier(voidMethodCallAST.method.classInstance)
            val method = getMethodFromClassInstance(instance as ObjectOrientedInstanceAST, voidMethodCallAST.method.name)
            val (params, dependents) = reconditionExpressionList(voidMethodCallAST.params)
            instanceDeps + dependents + VoidMethodCallAST(method, params + state)
        } else {
            val method = getReconditionedMethodSignature(voidMethodCallAST.method)
            val (params, dependents) = reconditionExpressionList(voidMethodCallAST.params)
            dependents + VoidMethodCallAST(method, params + listOf(state))
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
            is ModulusExpressionAST -> reconditionModulusExpression(expressionAST)
            is MultisetConversionAST -> reconditionMultisetConversion(expressionAST)
            is TernaryExpressionAST -> reconditionTernaryExpression(expressionAST)
            is IdentifierAST -> reconditionIdentifier(expressionAST)
            is IndexAST -> reconditionIndex(expressionAST)
            is IndexAssignAST -> reconditionIndexAssign(expressionAST)
            is ArrayInitAST -> reconditionArrayInit(expressionAST)
            is LiteralAST -> Pair(expressionAST, emptyList()) // do nothing
            is ClassInstantiationAST -> reconditionClassInstantiation(expressionAST)
            is ArrayLengthAST -> reconditionArrayLength(expressionAST)
            is NonVoidMethodCallAST -> reconditionNonVoidMethodCall(expressionAST)
            is FunctionMethodCallAST -> reconditionFunctionMethodCall(expressionAST)
            is SetDisplayAST -> reconditionSetDisplay(expressionAST)
            is SetComprehensionAST -> reconditionSetComprehension(expressionAST)
            is MapConstructorAST -> reconditionMapConstructor(expressionAST)
            is MapComprehensionAST -> reconditionMapComprehension(expressionAST)
            is SequenceDisplayAST -> reconditionSequenceDisplay(expressionAST)
            is SequenceComprehensionAST -> reconditionSeqeunceComprehension(expressionAST)
            is DatatypeInstantiationAST -> reconditionDatatypeInstantiation(expressionAST)
            is DatatypeUpdateAST -> reconditionDatatypeUpdate(expressionAST)
            is MatchExpressionAST -> reconditionMatchExpression(expressionAST)
        }

    fun reconditionDatatypeDestructor(destructorAST: DatatypeDestructorAST): Pair<DatatypeDestructorAST, List<StatementAST>> {
        val (instance, instanceDeps) = reconditionExpression(destructorAST.datatypeInstance)
        val (field, fieldDeps) = reconditionIdentifier(destructorAST.field)

        return Pair(DatatypeDestructorAST(instance, field), instanceDeps + fieldDeps)
    }

    fun reconditionDatatypeInstantiation(instantiationAST: DatatypeInstantiationAST): Pair<DatatypeInstantiationAST, List<StatementAST>> {
        val (params, paramDeps) = reconditionExpressionList(instantiationAST.params)

        return Pair(
            DatatypeInstantiationAST(instantiationAST.datatype, instantiationAST.constructor, params),
            paramDeps,
        )
    }

    fun reconditionDatatypeUpdate(updateAST: DatatypeUpdateAST): Pair<DatatypeUpdateAST, List<StatementAST>> {
        val (instance, instanceDeps) = reconditionExpression(updateAST.datatypeInstance)
        val (updates, updateDeps) = updateAST.updates.map { (ident, expr) ->
            val (rexpr, exprDeps) = reconditionExpression(expr)
            Pair(Pair(ident, rexpr), exprDeps)
        }.foldPair()

        return Pair(DatatypeUpdateAST(instance, updates), instanceDeps + updateDeps)
    }

    fun reconditionMatchExpression(matchExpressionAST: MatchExpressionAST): Pair<MatchExpressionAST, List<StatementAST>> {
        val (match, matchDeps) = reconditionExpression(matchExpressionAST.match)
        val (cases, caseDeps) = matchExpressionAST.cases.map { (case, expr) ->
            val (rexpr, exprDeps) = reconditionExpression(expr)
            Pair(Pair(case, rexpr), exprDeps)
        }.foldPair()

        return Pair(MatchExpressionAST(match, matchExpressionAST.type, cases), matchDeps + caseDeps)
    }

    fun reconditionBinaryExpression(binaryExpressionAST: BinaryExpressionAST): Pair<ExpressionAST, List<StatementAST>> {
        val (rexpr1, deps1) = reconditionExpression(binaryExpressionAST.expr1)
        val (rexpr2, deps2) = reconditionExpression(binaryExpressionAST.expr2)

        return when (binaryExpressionAST.operator) {
            DivisionOperator, ModuloOperator -> {
                val temp = IdentifierAST(tempGenerator.newValue(), binaryExpressionAST.type())

                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = binaryExpressionAST
                val methodCall = NonVoidMethodCallAST(
                    if (binaryExpressionAST.operator == DivisionOperator) ADVANCED_SAFE_DIV_INT.signature else ADVANCED_SAFE_MODULO_INT.signature,
                    listOf(rexpr1, rexpr2, state, StringLiteralAST(safetyId)),
                )

                Pair(temp, deps1 + deps2 + DeclarationAST(temp, methodCall))
            }

            else -> Pair(BinaryExpressionAST(rexpr1, binaryExpressionAST.operator, rexpr2), deps1 + deps2)
        }
    }

    fun reconditionUnaryExpression(unaryExpressionAST: UnaryExpressionAST): Pair<UnaryExpressionAST, List<StatementAST>> {
        val (reconditionedExpression, dependents) = reconditionExpression(unaryExpressionAST.expr)

        return Pair(UnaryExpressionAST(reconditionedExpression, unaryExpressionAST.operator), dependents)
    }

    fun reconditionModulusExpression(modulusExpressionAST: ModulusExpressionAST): Pair<ModulusExpressionAST, List<StatementAST>> {
        val (reconditionedExpression, dependents) = reconditionExpression(modulusExpressionAST.expr)
        return Pair(ModulusExpressionAST(reconditionedExpression), dependents)
    }

    fun reconditionMultisetConversion(multisetConversionAST: MultisetConversionAST): Pair<MultisetConversionAST, List<StatementAST>> {
        val (reconditionedExpression, dependents) = reconditionExpression(multisetConversionAST.expr)
        return Pair(MultisetConversionAST(reconditionedExpression), dependents)
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
        val reconditionedClass = reconditionClass(classInstantiationAST.clazz)
        val (params, dependents) = reconditionExpressionList(classInstantiationAST.params)

        return Pair(ClassInstantiationAST(reconditionedClass, params), dependents)
    }

    fun reconditionArrayInit(arrayInitAST: ArrayInitAST): Pair<ExpressionAST, List<StatementAST>> = when (arrayInitAST) {
        is ComprehensionInitialisedArrayInitAST -> reconditionComprehensionArrayInit(arrayInitAST)
        is ValueInitialisedArrayInitAST -> reconditionValueInitialisedArrayInit(arrayInitAST)
        else -> Pair(arrayInitAST, emptyList())
    }

    private fun reconditionValueInitialisedArrayInit(arrayInit: ValueInitialisedArrayInitAST): Pair<ArrayInitAST, List<StatementAST>> {
        val (exprs, exprDependents) = reconditionExpressionList(arrayInit.values)

        return Pair(ValueInitialisedArrayInitAST(arrayInit.length, exprs), exprDependents)
    }

    private fun reconditionComprehensionArrayInit(arrayInit: ComprehensionInitialisedArrayInitAST): Pair<ExpressionAST, List<StatementAST>> {
        val (expr, exprDependents) = reconditionExpression(arrayInit.expr)

        return if (exprDependents.isEmpty()) {
            Pair(ComprehensionInitialisedArrayInitAST(arrayInit.length, arrayInit.identifier, expr), emptyList())
        } else {
            val temp = IdentifierAST(tempGenerator.newValue(), arrayInit.type())
            val tempDecl = DeclarationAST(temp, ArrayInitAST(arrayInit.length, arrayInit.type()))
            val counter = arrayInit.identifier
            val loop = ForLoopAST(counter, IntegerLiteralAST(0), ArrayLengthAST(temp), SequenceAST(exprDependents + AssignmentAST(ArrayIndexAST(temp, counter), expr)))

            Pair(temp, listOf(tempDecl) + loop)
        }
    }

    fun reconditionArrayLength(arrayLengthAST: ArrayLengthAST): Pair<ArrayLengthAST, List<StatementAST>> {
        val (identifier, dependents) = reconditionIdentifier(arrayLengthAST.array)
        return Pair(ArrayLengthAST(identifier), dependents)
    }

    fun reconditionIdentifier(identifierAST: IdentifierAST): Pair<IdentifierAST, List<StatementAST>> =
        when (identifierAST) {
            is ArrayIndexAST -> {
                val (arr, arrDependents) = reconditionIdentifier(identifierAST.array)
                val (rexpr, exprDependents) = reconditionExpression(identifierAST.index)

                val temp = IdentifierAST(tempGenerator.newValue(), IntType)
                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = identifierAST
                val methodCall = NonVoidMethodCallAST(
                    ADVANCED_SAFE_ARRAY_INDEX.signature,
                    listOf(rexpr, ArrayLengthAST(arr), state, StringLiteralAST(safetyId)),
                )

                val decl = DeclarationAST(temp, methodCall)
                Pair(ArrayIndexAST(arr, temp), arrDependents + exprDependents + decl)
            }

            is ClassInstanceAST -> Pair(
                ClassInstanceAST(reconditionClass(identifierAST.clazz), identifierAST.name),
                emptyList(),
            )

            is TraitInstanceAST -> Pair(
                TraitInstanceAST(reconditionTrait(identifierAST.trait), identifierAST.name),
                emptyList(),
            )

            is DatatypeInstanceAST -> Pair(
                DatatypeInstanceAST(
                    identifierAST.name,
                    identifierAST.datatype,
                    identifierAST.mutable,
                    identifierAST.initialised(),
                ),
                emptyList(),
            )

            is TopLevelDatatypeInstanceAST -> Pair(
                TopLevelDatatypeInstanceAST(
                    identifierAST.name,
                    identifierAST.datatype,
                    identifierAST.mutable,
                    identifierAST.initialised(),
                ),
                emptyList(),
            )

            is ClassInstanceFieldAST -> {
                val (instance, instanceDependents) = reconditionIdentifier(identifierAST.classInstance)
                val (field, fieldDependents) = reconditionIdentifier(identifierAST.classField)

                Pair(ClassInstanceFieldAST(instance, field), instanceDependents + fieldDependents)
            }

            is DatatypeDestructorAST -> reconditionDatatypeDestructor(identifierAST)

            else -> Pair(IdentifierAST(identifierAST.name, reconditionType(identifierAST.type()), identifierAST.mutable, identifierAST.initialised()), emptyList())
        }

    fun reconditionIndex(indexAST: IndexAST): Pair<IndexAST, List<StatementAST>> = when (indexAST) {
        is MapIndexAST -> {
            val (map, mapDependents) = reconditionExpression(indexAST.map)
            val (key, keyDependents) = reconditionExpression(indexAST.key)

            Pair(MapIndexAST(map, key), mapDependents + keyDependents)
        }

        is MultisetIndexAST -> {
            val (multiset, multisetDependents) = reconditionExpression(indexAST.multiset)
            val (key, keyDependents) = reconditionExpression(indexAST.key)

            Pair(MultisetIndexAST(multiset, key), multisetDependents + keyDependents)
        }

        is SequenceIndexAST -> {
            val (seq, seqDependents) = reconditionExpression(indexAST.sequence)
            val (rexpr, exprDependents) = reconditionExpression(indexAST.key)

            val temp = IdentifierAST(tempGenerator.newValue(), IntType)
            val safetyId = safetyIdGenerator.newValue()
            idsMap[safetyId] = indexAST
            val methodCall = NonVoidMethodCallAST(
                ADVANCED_SAFE_ARRAY_INDEX.signature,
                listOf(rexpr, ModulusExpressionAST(seq), state, StringLiteralAST(safetyId)),
            )

            val decl = DeclarationAST(temp, methodCall)
            Pair(SequenceIndexAST(seq, temp), seqDependents + exprDependents + decl)
        }

        else -> throw UnsupportedOperationException()
    }

    fun reconditionIndexAssign(indexAssignAST: IndexAssignAST): Pair<IndexAssignAST, List<StatementAST>> {
        val (ident, identDependents) = reconditionExpression(indexAssignAST.expression)
        val (key, keyDependents) = reconditionExpression(indexAssignAST.key)
        val (value, valueDependents) = reconditionExpression(indexAssignAST.value)

        return when (ident.type()) {
            is MultisetType -> {
                val temp = IdentifierAST(tempGenerator.newValue(), IntType)
                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = indexAssignAST
                val methodCall =
                    NonVoidMethodCallAST(ADVANCED_ABSOLUTE.signature, listOf(value, state, StringLiteralAST(safetyId)))
                val decl = DeclarationAST(temp, methodCall)
                Pair(IndexAssignAST(ident, key, temp), identDependents + keyDependents + valueDependents + decl)
            }

            is SequenceType -> {
                val temp = IdentifierAST(tempGenerator.newValue(), IntType)
                val safetyId = safetyIdGenerator.newValue()
                idsMap[safetyId] = indexAssignAST
                val methodCall = NonVoidMethodCallAST(
                    ADVANCED_SAFE_ARRAY_INDEX.signature,
                    listOf(key, ModulusExpressionAST(ident), state, StringLiteralAST(safetyId)),
                )
                val decl = DeclarationAST(temp, methodCall)
                Pair(IndexAssignAST(ident, temp, value), identDependents + keyDependents + valueDependents + decl)
            }

            else -> Pair(IndexAssignAST(ident, key, value), identDependents + keyDependents + valueDependents)
        }
    }

    private fun getMethodFromClassInstance(instance: ObjectOrientedInstanceAST, methodName: String): MethodSignatureAST {
        val methodCandidates = instance.methods()
        return methodCandidates.first { it.name == methodName }
    }

    fun reconditionNonVoidMethodCall(nonVoidMethodCallAST: NonVoidMethodCallAST): Pair<NonVoidMethodCallAST, List<StatementAST>> =
        if (nonVoidMethodCallAST.method is ClassInstanceMethodSignatureAST) {
            val (instance, instanceDeps) = reconditionIdentifier(nonVoidMethodCallAST.method.classInstance)
            val method = getMethodFromClassInstance(instance as ObjectOrientedInstanceAST, nonVoidMethodCallAST.method.name)
            val (params, dependents) = reconditionExpressionList(nonVoidMethodCallAST.params)
            Pair(NonVoidMethodCallAST(method, params + state), instanceDeps + dependents)
        } else {
            val reconditionedMethod = getReconditionedMethodSignature(nonVoidMethodCallAST.method)
            val (params, dependents) = reconditionExpressionList(nonVoidMethodCallAST.params)
            Pair(NonVoidMethodCallAST(reconditionedMethod, params + state), dependents)
        }

    fun reconditionFunctionMethodCall(functionMethodCallAST: FunctionMethodCallAST): Pair<ExpressionAST, List<StatementAST>> =
        if (functionMethodCallAST.function is ClassInstanceFunctionMethodSignatureAST) {
            reconditionClassInstanceFunctionMethodCall(functionMethodCallAST)
        } else {
            val temp = when (val functionType = functionMethodCallAST.type()) {
                is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(tempGenerator.newValue(), functionType)
                else -> IdentifierAST(tempGenerator.newValue(), functionType)
            }
            val reconditionedMethod = getReconditionedFunctionMethodSignature(functionMethodCallAST.function)
            val (params, dependents) = reconditionExpressionList(functionMethodCallAST.params)

            val allDependents = dependents + listOf(
                DeclarationAST(temp, NonVoidMethodCallAST(reconditionedMethod, params + state)),
            )

            Pair(temp, allDependents)
        }

    fun reconditionClassInstanceFunctionMethodCall(functionMethodCallAST: FunctionMethodCallAST): Pair<ExpressionAST, List<StatementAST>> {
        val signature = functionMethodCallAST.function as ClassInstanceFunctionMethodSignatureAST
        val (instance, instanceDeps) = reconditionIdentifier(signature.classInstance)
        val methodCandidates = (instance as ObjectOrientedInstanceAST).methods()
        val reconditionedMethod = methodCandidates.first { signature.name == it.name }

        val (params, dependents) = reconditionExpressionList(functionMethodCallAST.params)
        val temp = when (val functionType = functionMethodCallAST.type()) {
            is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(tempGenerator.newValue(), functionType)
            else -> IdentifierAST(tempGenerator.newValue(), functionType)
        }

        return Pair(temp, instanceDeps + dependents + DeclarationAST(temp, NonVoidMethodCallAST(reconditionedMethod, params + state)))
    }

    fun reconditionSetDisplay(setDisplayAST: SetDisplayAST): Pair<ExpressionAST, List<StatementAST>> {
        val (exprs, exprDependents) = reconditionExpressionList(setDisplayAST.exprs)
        return Pair(SetDisplayAST(exprs, setDisplayAST.isMultiset), exprDependents)
    }

    fun reconditionSetComprehension(setComprehensionAST: SetComprehensionAST): Pair<ExpressionAST, List<StatementAST>> =
        when (setComprehensionAST) {
            is IntRangeSetComprehensionAST -> reconditionIntRangeSetComprehension(setComprehensionAST)
            is DataStructureSetComprehensionAST -> reconditionDataStructureSetComprehension(setComprehensionAST)
            else -> throw UnsupportedOperationException()
        }

    private fun reconditionIntRangeSetComprehension(setComprehensionAST: IntRangeSetComprehensionAST): Pair<ExpressionAST, List<StatementAST>> {
        val (bottomRange, bottomRangeDependents) = reconditionExpression(setComprehensionAST.bottomRange)
        val (topRange, topRangeDependents) = reconditionExpression(setComprehensionAST.topRange)
        val (expr, exprDependents) = reconditionExpression(setComprehensionAST.expr)

        return if (exprDependents.isEmpty()) {
            Pair(IntRangeSetComprehensionAST(setComprehensionAST.identifier, bottomRange, topRange, expr), bottomRangeDependents + topRangeDependents)
        } else {
            val temp = IdentifierAST(tempGenerator.newValue(), setComprehensionAST.type())
            val tempDecl = DeclarationAST(temp, SetDisplayAST(emptyList(), false, setComprehensionAST.type().innerType))
            val update = AssignmentAST(temp, BinaryExpressionAST(temp, UnionOperator, SetDisplayAST(listOf(expr), false)))
            val counter = setComprehensionAST.identifier
            val loop = ForLoopAST(counter, bottomRange, topRange, SequenceAST(exprDependents + update))

            Pair(temp, bottomRangeDependents + topRangeDependents + tempDecl + loop)
        }
    }

    private fun reconditionDataStructureSetComprehension(setComprehensionAST: DataStructureSetComprehensionAST): Pair<ExpressionAST, List<StatementAST>> {
        val (dataStructure, dataStructureDependents) = reconditionExpression(setComprehensionAST.dataStructure)
        val (expr, exprDependents) = reconditionExpression(setComprehensionAST.expr)

        return if (exprDependents.isEmpty()) {
            Pair(DataStructureSetComprehensionAST(setComprehensionAST.identifier, dataStructure, expr), dataStructureDependents)
        } else {
            val temp = IdentifierAST(tempGenerator.newValue(), setComprehensionAST.type())
            val tempDecl = DeclarationAST(temp, SetDisplayAST(emptyList(), false, setComprehensionAST.type()))
            val dataStructureType = dataStructure.type() as DataStructureType
            val (dataStructureTemp, dataStructureExpr) = if (dataStructureType is SequenceType || dataStructureType is MultisetType) {
                val counter = IdentifierAST(tempGenerator.newValue(), (dataStructure.type() as DataStructureType).innerType)
                Pair(
                    IdentifierAST(tempGenerator.newValue(), SetType(dataStructureType.innerType)),
                    DataStructureSetComprehensionAST(counter, dataStructure, counter),
                )
            } else {
                Pair(IdentifierAST(tempGenerator.newValue(), dataStructureType), dataStructure)
            }

            val dataStructureDecl = DeclarationAST(dataStructureTemp, dataStructureExpr)
            val assignSuchThat = DataStructureAssignSuchThatStatement(setComprehensionAST.identifier, dataStructureTemp)
            val tempUpdate = AssignmentAST(temp, BinaryExpressionAST(temp, UnionOperator, SetDisplayAST(listOf(setComprehensionAST.identifier), false)))
            val dataStructureUpdate = AssignmentAST(
                dataStructureTemp,
                BinaryExpressionAST(dataStructureTemp, DifferenceOperator, SetDisplayAST(listOf(setComprehensionAST.identifier), false)),
            )
            val loop = WhileLoopAST(
                BinaryExpressionAST(ModulusExpressionAST(dataStructureTemp), NotEqualsOperator, IntegerLiteralAST(0)),
                listOf(DecreasesAnnotation(ModulusExpressionAST(dataStructureTemp))),
                SequenceAST(listOf(assignSuchThat) + exprDependents + tempUpdate + dataStructureUpdate),
            )

            Pair(temp, dataStructureDependents + tempDecl + dataStructureDecl + loop)
        }
    }

    fun reconditionMapConstructor(mapConstructorAST: MapConstructorAST): Pair<ExpressionAST, List<StatementAST>> {
        val (assigns, assignDependents) = mapConstructorAST.assignments.map { (k, v) ->
            val (rk, kdeps) = reconditionExpression(k)
            val (rv, vdeps) = reconditionExpression(v)
            Pair(Pair(rk, rv), kdeps + vdeps)
        }.foldPair()

        return Pair(
            MapConstructorAST(reconditionType(mapConstructorAST.keyType), reconditionType(mapConstructorAST.valueType), assigns),
            assignDependents,
        )
    }

    fun reconditionMapComprehension(mapComprehensionAST: MapComprehensionAST): Pair<ExpressionAST, List<StatementAST>> =
        when (mapComprehensionAST) {
            is IntRangeMapComprehensionAST -> reconditionIntRangeMapComprehension(mapComprehensionAST)
            is DataStructureMapComprehensionAST -> reconditionDataStructureMapComprehension(mapComprehensionAST)
            else -> throw UnsupportedOperationException()
        }

    private fun reconditionIntRangeMapComprehension(mapComprehensionAST: IntRangeMapComprehensionAST): Pair<ExpressionAST, List<StatementAST>> {
        val (bottomRange, bottomRangeDependents) = reconditionExpression(mapComprehensionAST.bottomRange)
        val (topRange, topRangeDependents) = reconditionExpression(mapComprehensionAST.topRange)
        val (key, keyDependents) = reconditionExpression(mapComprehensionAST.assign.first)
        val (value, valueDependents) = reconditionExpression(mapComprehensionAST.assign.second)

        val internalDependents = keyDependents + valueDependents

        return if (internalDependents.isEmpty()) {
            Pair(IntRangeMapComprehensionAST(mapComprehensionAST.identifier, bottomRange, topRange, Pair(key, value)), bottomRangeDependents + topRangeDependents)
        } else {
            val temp = IdentifierAST(tempGenerator.newValue(), mapComprehensionAST.type())
            val tempDecl = DeclarationAST(temp, MapConstructorAST(mapComprehensionAST.type().keyType, mapComprehensionAST.type().valueType))
            val update = AssignmentAST(temp, IndexAssignAST(temp, key, value))
            val counter = mapComprehensionAST.identifier
            val loop = ForLoopAST(counter, bottomRange, topRange, SequenceAST(internalDependents + update))
            Pair(temp, bottomRangeDependents + topRangeDependents + tempDecl + loop)
        }
    }

    private fun reconditionDataStructureMapComprehension(mapComprehensionAST: DataStructureMapComprehensionAST): Pair<ExpressionAST, List<StatementAST>> {
        val (dataStructure, dataStructureDependents) = reconditionExpression(mapComprehensionAST.dataStructure)
        val (key, keyDependents) = reconditionExpression(mapComprehensionAST.assign.first)
        val (value, valueDependents) = reconditionExpression(mapComprehensionAST.assign.second)

        val internalDependents = keyDependents + valueDependents

        return if (internalDependents.isEmpty()) {
            Pair(DataStructureMapComprehensionAST(mapComprehensionAST.identifier, dataStructure, Pair(key, value)), dataStructureDependents)
        } else {
            val temp = IdentifierAST(tempGenerator.newValue(), mapComprehensionAST.type())
            val tempDecl = DeclarationAST(temp, MapConstructorAST(mapComprehensionAST.type().keyType, mapComprehensionAST.type().valueType))
            val dataStructureType = dataStructure.type() as DataStructureType
            val (dataStructureTemp, dataStructureExpr) = if (dataStructureType is SequenceType || dataStructureType is MultisetType) {
                val counter = IdentifierAST(tempGenerator.newValue(), (dataStructure.type() as DataStructureType).innerType)
                Pair(
                    IdentifierAST(tempGenerator.newValue(), SetType(dataStructureType.innerType)),
                    DataStructureSetComprehensionAST(counter, dataStructure, counter),
                )
            } else {
                Pair(IdentifierAST(tempGenerator.newValue(), dataStructureType), dataStructure)
            }

            val dataStructureDecl = DeclarationAST(dataStructureTemp, dataStructureExpr)
            val assignSuchThat = DataStructureAssignSuchThatStatement(mapComprehensionAST.identifier, dataStructureTemp)
            val tempUpdate = AssignmentAST(temp, IndexAssignAST(temp, key, value))
            val dataStructureUpdate =
                AssignmentAST(dataStructureTemp, BinaryExpressionAST(dataStructureTemp, DifferenceOperator, SetDisplayAST(listOf(mapComprehensionAST.identifier), false)))
            val loop = WhileLoopAST(
                BinaryExpressionAST(ModulusExpressionAST(dataStructureTemp), NotEqualsOperator, IntegerLiteralAST(0)),
                listOf(DecreasesAnnotation(ModulusExpressionAST(dataStructureTemp))),
                SequenceAST(listOf(assignSuchThat) + internalDependents + tempUpdate + dataStructureUpdate),
            )

            Pair(temp, dataStructureDependents + tempDecl + dataStructureDecl + loop)
        }
    }

    fun reconditionSequenceDisplay(sequenceDisplayAST: SequenceDisplayAST): Pair<SequenceDisplayAST, List<StatementAST>> {
        val (exprs, exprDependents) = reconditionExpressionList(sequenceDisplayAST.exprs)
        return Pair(SequenceDisplayAST(exprs), exprDependents)
    }

    fun reconditionSeqeunceComprehension(
        sequenceComprehensionAST: SequenceComprehensionAST,
    ): Pair<ExpressionAST, List<StatementAST>> {
        val temp = IdentifierAST(tempGenerator.newValue(), IntType)
        val safetyId = safetyIdGenerator.newValue()
        idsMap[safetyId] = sequenceComprehensionAST
        val methodCall = NonVoidMethodCallAST(ADVANCED_ABSOLUTE.signature, listOf(sequenceComprehensionAST.size, state, StringLiteralAST(safetyId)))
        val tempDecl = DeclarationAST(temp, methodCall)
        val (expr, exprDependents) = reconditionExpression(sequenceComprehensionAST.expr)

        return if (exprDependents.isEmpty()) {
            Pair(SequenceComprehensionAST(temp, sequenceComprehensionAST.identifier, sequenceComprehensionAST.annotations, expr), listOf(tempDecl))
        } else {
            val tempSeq = IdentifierAST(tempGenerator.newValue(), sequenceComprehensionAST.type())
            val tempSeqDecl = DeclarationAST(tempSeq, SequenceDisplayAST(emptyList(), sequenceComprehensionAST.type().innerType))
            val update = AssignmentAST(tempSeq, BinaryExpressionAST(tempSeq, UnionOperator, SequenceDisplayAST(listOf(expr))))
            val counter = sequenceComprehensionAST.identifier
            val loop = ForLoopAST(counter, IntegerLiteralAST(0), temp, SequenceAST(exprDependents + update))

            Pair(tempSeq, listOf(tempDecl) + tempSeqDecl + loop)
        }
    }

    fun reconditionType(type: Type): Type = when (type) {
        is ClassType -> ClassType(reconditionClass(type.clazz))
        is TraitType -> TraitType(reconditionTrait(type.trait))
        is DatatypeType -> {
            val datatype = reconditionDatatype(type.datatype)
            DatatypeType(datatype, type.constructor)
        }

        is ArrayType -> ArrayType(reconditionType(type.internalType))
        is MapType -> MapType(reconditionType(type.keyType), reconditionType(type.valueType))
        is MultisetType -> MultisetType(reconditionType(type.innerType))
        is SequenceType -> SequenceType(reconditionType(type.innerType))
        is SetType -> SetType(reconditionType(type.innerType))

        else -> type
    }

    companion object {
        private const val FM_RETURNS = "r1"
        private const val STATE_NAME = "advancedState"

        private val STATE_MAP_TYPE = MapType(StringType, BoolType)
        private val STATE_TYPE = ClassType(ADVANCED_RECONDITION_CLASS)

        private val state = ClassInstanceAST(ADVANCED_RECONDITION_CLASS, STATE_NAME)

        private val additionalParams = listOf(state)
    }
}
