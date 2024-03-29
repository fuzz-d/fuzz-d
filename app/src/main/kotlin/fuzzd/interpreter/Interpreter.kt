package fuzzd.interpreter

import fuzzd.generator.ast.ClassInstanceFunctionMethodSignatureAST
import fuzzd.generator.ast.ClassInstanceMethodSignatureAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
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
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
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
import fuzzd.generator.ast.StatementAST.AssignSuchThatStatement
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.ConjunctiveAssertStatement
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.DataStructureAssignSuchThatStatement
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
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.VerifierAnnotationAST.EnsuresAnnotation
import fuzzd.generator.ast.VerifierAnnotationAST.InvariantAnnotation
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.AntiMembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureEqualityOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureInequalityOperator
import fuzzd.generator.ast.operators.BinaryOperator.DifferenceOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjointOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.IffOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.IntersectionOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.ProperSubsetOperator
import fuzzd.generator.ast.operators.BinaryOperator.ProperSupersetOperator
import fuzzd.generator.ast.operators.BinaryOperator.ReverseImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubsetOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import fuzzd.generator.ast.operators.BinaryOperator.SupersetOperator
import fuzzd.generator.ast.operators.BinaryOperator.UnionOperator
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.ArrayValue
import fuzzd.interpreter.value.Value.BoolValue
import fuzzd.interpreter.value.Value.CharValue
import fuzzd.interpreter.value.Value.ClassValue
import fuzzd.interpreter.value.Value.DataStructureValue
import fuzzd.interpreter.value.Value.DatatypeValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.MapValue
import fuzzd.interpreter.value.Value.MultiValue
import fuzzd.interpreter.value.Value.MultisetValue
import fuzzd.interpreter.value.Value.SequenceValue
import fuzzd.interpreter.value.Value.SetValue
import fuzzd.interpreter.value.Value.StringValue
import fuzzd.interpreter.value.Value.TopLevelDatatypeValue
import fuzzd.interpreter.value.ValueTable
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.ADVANCED_ABSOLUTE
import fuzzd.utils.ADVANCED_SAFE_DIV_INT
import fuzzd.utils.ADVANCED_SAFE_INDEX
import fuzzd.utils.ADVANCED_SAFE_MODULO_INT
import fuzzd.utils.REQUIRE_SAFETY_ID
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_INDEX
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.conjunct
import fuzzd.utils.mapFirst
import fuzzd.utils.reduceLists
import fuzzd.utils.toMultiset
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO
import java.math.BigInteger.valueOf

class Interpreter(val generateChecksum: Boolean, val verify: Boolean = false) : ASTInterpreter {
    private val output = StringBuilder()
    private var doBreak = false

    /* ============================== TOP LEVEL ============================== */
    override fun interpretDafny(dafny: DafnyAST): Pair<String, List<StatementAST>> {
        val context = InterpreterContext()
        dafny.topLevelElements.filterIsInstance<MethodAST>().forEach { method ->
            context.methods.assign(method.signature, method.getBody())
        }

        dafny.topLevelElements.filterIsInstance<FunctionMethodAST>().forEach { function ->
            context.functions.assign(function.signature, function.getBody())
        }

        listOf(
            REQUIRE_SAFETY_ID,
            ADVANCED_ABSOLUTE,
            ADVANCED_SAFE_INDEX,
            ADVANCED_SAFE_DIV_INT,
            ADVANCED_SAFE_MODULO_INT,
        ).forEach { context.methods.assign(it.signature, it.getBody()) }

        listOf(
            ABSOLUTE,
            SAFE_INDEX,
            SAFE_DIVISION_INT,
            SAFE_MODULO_INT,
        ).forEach { context.functions.assign(it.signature, it.getBody()) }

        val mainFunction = dafny.topLevelElements.first { it is MainFunctionAST }
        val prints = interpretMainFunction(mainFunction as MainFunctionAST, context)
        return Pair(output.toString(), prints)
    }

    override fun interpretMainFunction(mainFunction: MainFunctionAST, context: InterpreterContext): List<StatementAST> {
        interpretSequence(mainFunction.sequenceAST, context)

        // generate checksum prints
        val prints = if (generateChecksum) {
            context.fields.values
                .filter { (_, v) -> v != null }
                .map { (k, v) ->
                    generateChecksumPrint(k, v!!, context)
                }.reduceLists()
        } else {
            emptyList()
        }

        prints.forEach { interpretPrint(it, context) }
        return prints
    }

    private fun generateChecksumPrint(key: IdentifierAST, value: Value, context: InterpreterContext): List<PrintAST> =
        when (value) {
            is MultiValue, is CharValue, is StringValue, is IntValue, is BoolValue -> listOf(PrintAST(key))
            is SetValue, is MultisetValue, is MapValue, is SequenceValue -> {
                if (key.type().hasHeapType()) {
                    listOf(PrintAST(ModulusExpressionAST(key)))
                } else {
                    listOf(PrintAST(BinaryExpressionAST(key, DataStructureEqualityOperator, value.toExpressionAST())))
                }
            }

            is ArrayValue -> {
                val indices = value.arr.indices.filter { i -> value.arr[i] != null }
                indices.map { i ->
                    val identifier = ArrayIndexAST(key, IntegerLiteralAST(i))
                    generateChecksumPrint(identifier, interpretIdentifier(identifier, context), context)
                }.reduceLists()
            }

            is ClassValue -> {
                when (key) {
                    is ObjectOrientedInstanceAST -> key.fields().map { generateChecksumPrint(it, interpretIdentifier(it, context), context) }.reduceLists()
                    is DatatypeDestructorAST, is ArrayIndexAST -> {
                        val keyType = key.type()
                        val fields = if (keyType is ClassType) {
                            keyType.clazz.constructorFields
                        } else {
                            (keyType as TraitType).trait.fields()
                        }

                        fields.map {
                            val classInstanceField = ClassInstanceFieldAST(key, it)
                            generateChecksumPrint(classInstanceField, interpretIdentifier(classInstanceField, context), context)
                        }.reduceLists()
                    }

                    else -> throw UnsupportedOperationException()
                }
            }

            is TopLevelDatatypeValue -> {
                value.fields().map { field ->
                    val destructor = DatatypeDestructorAST(key, field)
                    generateChecksumPrint(destructor, interpretIdentifier(destructor, context), context)
                }.reduceLists()
            }
        }

    override fun interpretSequence(sequence: SequenceAST, context: InterpreterContext) {
        sequence.setLive()
        sequence.statements.forEach { interpretStatement(it, context) }
    }

    /* ============================== STATEMENTS ============================= */

    override fun interpretStatement(statement: StatementAST, context: InterpreterContext) {
        if (doBreak) return

        return when (statement) {
            is BreakAST -> {
                doBreak = true
            }

            is AssignSuchThatStatement -> interpretAssignSuchThatStatement(statement, context)
            is MatchStatementAST -> interpretMatchStatement(statement, context)
            is IfStatementAST -> interpretIfStatement(statement, context)
            is ForallStatementAST -> interpretForallStatement(statement, context)
            is ForLoopAST -> interpretForLoopStatement(statement, context)
            is VerificationAwareWhileLoopAST -> interpretVerificationAwareWhileStatement(statement, context)
            is CounterLimitedWhileLoopAST -> interpretCounterLimitedWhileStatement(statement, context)
            is WhileLoopAST -> interpretWhileStatement(statement, context)
            is VoidMethodCallAST -> interpretVoidMethodCall(statement, context)
            is MultiTypedDeclarationAST -> interpretMultiTypedDeclaration(statement, context)
            is MultiDeclarationAST -> interpretMultiDeclaration(statement, context)
            is MultiAssignmentAST -> interpretMultiAssign(statement, context)
            is PrintAST -> interpretPrint(statement, context)
            is ConjunctiveAssertStatement -> interpretConjunctiveAssertStatement(statement, context)
            is AssertStatementAST -> interpretAssertStatement(statement, context)
        }
    }

    // specific function for handling assertions in methods & while loops where parameters may/may not have an influence on the assertion outcome.
    // if they do, it generates an assertion for the current parameter values (<p1> == ... && <p2> == ... && ... ==> <assertion>)
    override fun interpretConjunctiveAssertStatement(assertStatement: ConjunctiveAssertStatement, context: InterpreterContext) {
        val baseExpr = (assertStatement.baseExpr as BinaryExpressionAST).expr1
        val baseExprValue = interpretExpression(baseExpr, context).toExpressionAST()

        if (context.annotationIdentifiers.isNotEmpty()) {
            val identifierValues = context.annotationIdentifiers.toSet().map { Pair(it, interpretIdentifier(it, context)) }
            val identifierCondition = identifierValues
                .map { (identifier, value) -> BinaryExpressionAST(identifier, EqualsOperator, value.toExpressionAST()) }
                .reduce { l, r -> BinaryExpressionAST(l, ConjunctionOperator, r) }
            val condition = BinaryExpressionAST(identifierCondition, ImplicationOperator, BinaryExpressionAST(baseExpr, EqualsOperator, baseExprValue))
            assertStatement.exprs.add(condition)
        } else {
            if (assertStatement.exprs.isEmpty()) {
                // We don't need to do a disjunction since the assertion will be the same for all method invocations
                assertStatement.exprs.add(BinaryExpressionAST(baseExpr, EqualsOperator, baseExprValue))
            }
        }
    }

    override fun interpretAssertStatement(assertStatement: AssertStatementAST, context: InterpreterContext) {
        val identifier = (assertStatement.expr as BinaryExpressionAST).expr1
        val identifierValue = interpretExpression(identifier, context).toExpressionAST()
        assertStatement.expr = BinaryExpressionAST(identifier, EqualsOperator, identifierValue)
    }

    override fun interpretAssignSuchThatStatement(statement: AssignSuchThatStatement, context: InterpreterContext) =
        if (statement is DataStructureAssignSuchThatStatement) {
            val dataStructure = interpretExpression(statement.dataStructure, context) as DataStructureValue
            context.fields.assign(statement.identifier, dataStructure.elements().first())
        } else {
            throw UnsupportedOperationException()
        }

    override fun interpretMatchStatement(matchStatement: MatchStatementAST, context: InterpreterContext) {
        val datatypeValue = interpretExpression(matchStatement.match, context) as DatatypeValue
        val (_, seq) = matchStatement.cases.map { (case, seq) ->
            val datatypeType = case.type() as DatatypeType
            Pair(datatypeType, seq)
        }.first { (type, _) -> datatypeValue.constructor == type.constructor }

        val caseContext = context.increaseDepth()
        datatypeValue.constructor.fields.forEach {
            if (datatypeValue.values.has(it)) {
                caseContext.fields.declare(it, datatypeValue.values.get(it)!!)
            } else {
                caseContext.fields.create(it)
            }
        }
        interpretSequence(seq, caseContext)
    }

    override fun interpretIfStatement(ifStatement: IfStatementAST, context: InterpreterContext) {
        val conditionValue = interpretExpression(ifStatement.condition, context) as BoolValue

        if (conditionValue.value) {
            interpretSequence(ifStatement.ifBranch, context.increaseDepth())
        } else {
            ifStatement.elseBranch?.let { interpretSequence(it, context.increaseDepth()) }
        }
    }

    private fun createInvariantForModset(
        counter: IdentifierAST,
        condition: ExpressionAST,
        modset: Set<IdentifierAST>,
        context: InterpreterContext,
    ): InvariantAnnotation {
        val counterValue = interpretIdentifier(counter, context) as IntValue
        val conditionValue = interpretExpression(condition, context) as BoolValue
        val modsetValues = modset.map { Pair(it, interpretExpression(it, context)) }

        val identifierValues = context.annotationIdentifiers
            .map { Pair(it, interpretIdentifier(it, context).toExpressionAST()) }
            .map { BinaryExpressionAST(it.first, EqualsOperator, it.second) }
        val counterCondition = BinaryExpressionAST(counter, EqualsOperator, counterValue.toExpressionAST())
        val implicationCondition = if (identifierValues.isEmpty()) {
            counterCondition
        } else {
            BinaryExpressionAST(counterCondition, ConjunctionOperator, identifierValues.conjunct())
        }

        val modsetConditions = modsetValues.map { (ident, value) -> BinaryExpressionAST(ident, EqualsOperator, value.toExpressionAST()) }
        return InvariantAnnotation(
            BinaryExpressionAST(
                implicationCondition,
                ImplicationOperator,
                BinaryExpressionAST(
                    BinaryExpressionAST(condition, EqualsOperator, conditionValue.toExpressionAST()),
                    ConjunctionOperator,
                    modsetConditions.conjunct(),
                ),
            ),
        )
    }

    override fun interpretVerificationAwareWhileStatement(whileStatement: VerificationAwareWhileLoopAST, context: InterpreterContext) {
        interpretStatement(whileStatement.counterInitialisation, context)
        var condition = interpretExpression(whileStatement.condition, context)
        val prevBreak = doBreak
        doBreak = false
        whileStatement.invariants.add(createInvariantForModset(whileStatement.counter, whileStatement.condition, whileStatement.modset.toSet(), context))
        while ((condition as BoolValue).value && !doBreak) {
            val innerContext = context.increaseDepth().withAnnotationIdentifiers(whileStatement.modset)
            interpretStatement(whileStatement.terminationCheck, innerContext)
            if (doBreak) {
                break
            }

            interpretStatement(whileStatement.counterUpdate, innerContext)
            interpretSequence(whileStatement.body, innerContext)
            condition = interpretExpression(whileStatement.condition, context)
            whileStatement.invariants.add(createInvariantForModset(whileStatement.counter, whileStatement.condition, whileStatement.modset.toSet(), context))
        }

        doBreak = prevBreak
    }

    override fun interpretCounterLimitedWhileStatement(
        whileStatement: CounterLimitedWhileLoopAST,
        context: InterpreterContext,
    ) {
        interpretStatement(whileStatement.counterInitialisation, context)
        var condition = interpretExpression(whileStatement.condition, context)
        val prevBreak = doBreak
        doBreak = false
        while ((condition as BoolValue).value && !doBreak) {
            val innerContext = context.increaseDepth()
            interpretStatement(whileStatement.terminationCheck, innerContext)
            if (doBreak) {
                break
            }
            interpretStatement(whileStatement.counterUpdate, innerContext)
            interpretSequence(whileStatement.body, innerContext)
            condition = interpretExpression(whileStatement.condition, context)
        }
        doBreak = prevBreak
    }

    override fun interpretForLoopStatement(forLoop: ForLoopAST, context: InterpreterContext) {
        val bottomRange = interpretExpression(forLoop.bottomRange, context) as IntValue
        val topRange = interpretExpression(forLoop.topRange, context) as IntValue

        var i = bottomRange.value
        while (i < topRange.value) {
            val sequenceContext = context.increaseDepth()
            sequenceContext.fields.declare(forLoop.identifier, IntValue(i))
            interpretSequence(forLoop.body, sequenceContext)
            i += ONE
        }
    }

    override fun interpretForallStatement(forallStatement: ForallStatementAST, context: InterpreterContext) {
        val bottomRange = interpretExpression(forallStatement.bottomRange, context) as IntValue
        val topRange = interpretExpression(forallStatement.topRange, context) as IntValue

        var i = bottomRange.value
        while (i < topRange.value) {
            val assignContext = context.increaseDepth()
            assignContext.fields.declare(forallStatement.identifier, IntValue(i))
            interpretMultiAssign(forallStatement.assignment, assignContext)
            i += ONE
        }
    }

    override fun interpretWhileStatement(whileStatement: WhileLoopAST, context: InterpreterContext) {
        var condition = interpretExpression(whileStatement.condition, context)
        val prevBreak = doBreak
        doBreak = false
        while ((condition as BoolValue).value && !doBreak) {
            interpretSequence(whileStatement.body, context.increaseDepth())
            condition = interpretExpression(whileStatement.condition, context)
        }
        doBreak = prevBreak
    }

    override fun interpretVoidMethodCall(methodCall: VoidMethodCallAST, context: InterpreterContext) {
        interpretMethodCall(methodCall.method, methodCall.params, emptyList(), context)
    }

    override fun interpretMultiTypedDeclaration(
        typedDeclaration: MultiTypedDeclarationAST,
        context: InterpreterContext,
    ) {
        val values = typedDeclaration.exprs.map { interpretExpression(it, context) }
        typedDeclaration.identifiers.indices.forEach { i ->
            setIdentifierValue(typedDeclaration.identifiers[i], values[i], context, true)
        }
    }

    override fun interpretMultiDeclaration(declaration: MultiDeclarationAST, context: InterpreterContext) {
        if (declaration.exprs.size == 1 && declaration.exprs[0] is NonVoidMethodCallAST) {
            // non void method call
            val methodReturns = interpretExpression(declaration.exprs[0], context) as MultiValue
            declaration.identifiers.indices.forEach { i ->
                setIdentifierValue(declaration.identifiers[i], methodReturns.values[i], context, true)
            }
        } else {
            val values = declaration.exprs.map { interpretExpression(it, context) }
            declaration.identifiers.indices.forEach { i ->
                setIdentifierValue(declaration.identifiers[i], values[i], context, true)
            }
        }
    }

    override fun interpretMultiAssign(assign: MultiAssignmentAST, context: InterpreterContext) {
        val values = assign.exprs.map { interpretExpression(it, context) }
        assign.identifiers.indices.forEach { i ->
            setIdentifierValue(assign.identifiers[i], values[i], context, false)
        }
    }

    private fun setIdentifierValue(
        identifier: IdentifierAST,
        value: Value,
        context: InterpreterContext,
        isDeclaration: Boolean,
    ) {
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, context) as ClassValue
                setIdentifierValue(identifier.classField, value, context.withClassContext(classValue.classContext), isDeclaration)
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, context) as ArrayValue
                val index = interpretExpression(identifier.index, context) as IntValue
                arrayValue.setIndex(index.value.toInt(), value)
            }

            else -> if (isDeclaration) {
                context.fields.declare(identifier, value)
            } else {
                if (context.fields.has(identifier)) {
                    context.fields.assign(identifier, value)
                } else {
                    context.classContext!!.fields.assign(identifier, value)
                }
            }
        }
    }

    override fun interpretPrint(printAST: PrintAST, context: InterpreterContext) {
        val values = printAST.expr.map { interpretExpression(it, context) }
        values.forEach {
            emitOutput(it)
            if (printAST.newLine) output.appendLine()
        }
    }

    private fun emitOutput(value: Value) {
        when (value) {
            is TopLevelDatatypeValue -> emitDatatypeValue(value)
            is ClassValue -> emitClassValue(value)
            is ArrayValue -> emitArrayValue(value)
            is SetValue -> emitSetValue(value)
            is MultisetValue -> emitMultisetValue(value)
            is MultiValue -> emitMultiValue(value)
            is MapValue -> emitMapValue(value)
            is SequenceValue -> emitSequenceValue(value)
            is StringValue -> emitStringValue(value)
            is CharValue -> emitCharValue(value)
            is IntValue -> emitIntValue(value)
            is BoolValue -> emitBoolValue(value)
        }
    }

    private fun emitDatatypeValue(datatypeValue: TopLevelDatatypeValue) {
        throw UnsupportedOperationException()
    }

    private fun emitClassValue(classValue: ClassValue) {
        classValue.classContext.fields.values
            .filter { (_, v) -> v != null }
            .forEach { (_, v) ->
                emitOutput(v!!)
            }
    }

    private fun emitArrayValue(arrayValue: ArrayValue) {
        arrayValue.arr.filterNotNull().forEach { emitOutput(it) }
    }

    private fun emitSetValue(setValue: SetValue) {
        setValue.set.forEach {
            emitOutput(it)
            emitOutput(StringValue("\n"))
        }
    }

    private fun emitMultisetValue(multisetValue: MultisetValue) {
        multisetValue.map.forEach { (k, v) ->
            emitOutput(k)
            output.append(v)
        }
    }

    private fun emitMapValue(mapValue: MapValue) {
        mapValue.map.forEach { (k, v) ->
            emitOutput(k)
            emitOutput(v)
        }
    }

    private fun emitMultiValue(multiValue: MultiValue) {
        multiValue.values.forEach { emitOutput(it) }
    }

    private fun emitSequenceValue(sequenceValue: SequenceValue) {
        if (sequenceValue.seq.isNotEmpty() && sequenceValue.seq[0] is CharValue) {
            sequenceValue.seq.forEach { emitSeqCharValue(it as CharValue) }
        } else {
            sequenceValue.seq.forEach { emitOutput(it) }
        }
    }

    private fun emitStringValue(stringValue: StringValue) {
        output.append(stringValue.value)
    }

    private fun emitSeqCharValue(charValue: CharValue) {
        output.append("${charValue.value}")
    }

    private fun emitCharValue(charValue: CharValue) {
        output.append("'${charValue.value}'")
    }

    private fun emitIntValue(intValue: IntValue) {
        output.append(intValue.value)
    }

    private fun emitBoolValue(boolValue: BoolValue) {
        output.append(boolValue.value)
    }

    /* ============================== EXPRESSIONS ============================ */
    override fun interpretExpression(expression: ExpressionAST, context: InterpreterContext): Value {
        return when (expression) {
            is FunctionMethodCallAST -> interpretFunctionMethodCall(expression, context)
            is NonVoidMethodCallAST -> interpretNonVoidMethodCall(expression, context)
            is ClassInstantiationAST -> interpretClassInstantiation(expression, context)
            is BinaryExpressionAST -> interpretBinaryExpression(expression, context)
            is TernaryExpressionAST -> interpretTernaryExpression(expression, context)
            is UnaryExpressionAST -> interpretUnaryExpression(expression, context)
            is ModulusExpressionAST -> interpretModulus(expression, context)
            is MultisetConversionAST -> interpretMultisetConversion(expression, context)
            is IdentifierAST -> interpretIdentifier(expression, context)
            is IndexAST -> interpretIndex(expression, context)
            is IndexAssignAST -> interpretIndexAssign(expression, context)
            is SetDisplayAST -> interpretSetDisplay(expression, context)
            is SetComprehensionAST -> interpretSetComprehension(expression, context)
            is SequenceDisplayAST -> interpretSequenceDisplay(expression, context)
            is SequenceComprehensionAST -> interpretSequenceComprehension(expression, context)
            is MapConstructorAST -> interpretMapConstructor(expression, context)
            is MapComprehensionAST -> interpretMapComprehension(expression, context)
            is ArrayLengthAST -> interpretArrayLength(expression, context)
            is ArrayInitAST -> interpretArrayInit(expression, context)
            is CharacterLiteralAST -> interpretCharacterLiteral(expression, context)
            is StringLiteralAST -> interpretStringLiteral(expression, context)
            is IntegerLiteralAST -> interpretIntegerLiteral(expression, context)
            is BooleanLiteralAST -> interpretBooleanLiteral(expression, context)
            is DatatypeInstantiationAST -> interpretDatatypeInstantiation(expression, context)
            is DatatypeUpdateAST -> interpretDatatypeUpdate(expression, context)
            is MatchExpressionAST -> interpretMatchExpression(expression, context)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun interpretDatatypeInstantiation(
        instantiation: DatatypeInstantiationAST,
        context: InterpreterContext,
    ): Value {
        val constructor = instantiation.constructor
        val identifiers = constructor.fields

        val values = instantiation.params.map { interpretExpression(it, context) }
        val datatypeValueTable = ValueTable<IdentifierAST, Value?>()

        instantiation.datatype.constructors.forEach { c -> c.fields.forEach { datatypeValueTable.create(it) } }
        identifiers.zip(values).forEach { (identifier, value) -> datatypeValueTable.declare(identifier, value) }

        return DatatypeValue(instantiation.datatype, constructor, datatypeValueTable)
    }

    override fun interpretDatatypeUpdate(update: DatatypeUpdateAST, context: InterpreterContext): Value {
        val datatypeValue = interpretExpression(update.datatypeInstance, context) as TopLevelDatatypeValue
        val constructor = datatypeValue.datatype.constructors.first { it.fields.containsAll(update.updates.mapFirst()) }
        val assigns = update.updates.map { (identifier, expr) -> Pair(identifier, interpretExpression(expr, context)) }
        return DatatypeValue(datatypeValue.datatype, constructor, datatypeValue.values).assign(assigns)
    }

    override fun interpretDatatypeDestructor(destructor: DatatypeDestructorAST, context: InterpreterContext): Value {
        val datatypeValue = interpretExpression(destructor.datatypeInstance, context) as TopLevelDatatypeValue
        return datatypeValue.values.get(destructor.field)!! // might need to swap to interpretIdentifier
    }

    override fun interpretMatchExpression(matchExpression: MatchExpressionAST, context: InterpreterContext): Value {
        val datatypeValue = interpretExpression(matchExpression.match, context) as DatatypeValue
        val (_, expr) = matchExpression.cases.map { (case, seq) ->
            val datatypeType = case.type() as DatatypeType
            Pair(datatypeType, seq)
        }.first { (type, _) -> datatypeValue.constructor == type.constructor }

        val caseContext = context.increaseDepth()
        datatypeValue.constructor.fields.forEach {
            if (datatypeValue.values.has(it)) {
                caseContext.fields.declare(it, datatypeValue.values.get(it)!!)
            } else {
                caseContext.fields.create(it)
            }
        }

        return interpretExpression(expr, caseContext)
    }

    override fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST, context: InterpreterContext): Value =
        if (functionCall.function is ClassInstanceFunctionMethodSignatureAST) {
            val functionSignature = functionCall.function
            val classValue = interpretIdentifier(functionSignature.classInstance, context) as ClassValue
            val functionContext = InterpreterContext(
                ValueTable(classValue.classContext.fields),
                context.functions,
                context.methods,
                classValue.classContext,
            )

            setParams(functionSignature.params, functionCall.params, functionContext.fields, context)

            val body = classValue.classContext.functions.get(functionSignature.signature)
            interpretExpression(body, functionContext)
        } else {
            interpretFunctionCall(functionCall.function, functionCall.params, context)
        }

    private fun interpretFunctionCall(
        functionSignature: FunctionMethodSignatureAST,
        params: List<ExpressionAST>,
        context: InterpreterContext,
    ): Value {
        val (functionContext, body) = if (context.functions.has(functionSignature)) {
            Pair(
                InterpreterContext(ValueTable(), context.functions, context.methods, null),
                context.functions.get(functionSignature),
            )
        } else {
            Pair(
                InterpreterContext(
                    ValueTable(context.classContext!!.fields),
                    context.functions,
                    context.methods,
                    context.classContext,
                ),
                context.classContext.functions.get(functionSignature),
            )
        }

        setParams(functionSignature.params, params, functionContext.fields, context)

        return interpretExpression(body, functionContext)
    }

    private fun setParams(
        functionParams: List<IdentifierAST>,
        functionCallParams: List<ExpressionAST>,
        functionFields: ValueTable<IdentifierAST, Value>,
        context: InterpreterContext,
    ) {
        functionParams.indices.forEach { i ->
            functionFields.declare(functionParams[i], interpretExpression(functionCallParams[i], context))
        }
    }

    private fun interpretClassInstanceMethodCall(
        methodSignature: ClassInstanceMethodSignatureAST,
        params: List<ExpressionAST>,
        returns: List<IdentifierAST>,
        context: InterpreterContext,
    ): ValueTable<IdentifierAST, Value> {
        val classValue = interpretIdentifier(methodSignature.classInstance, context) as ClassValue
        val methodContext = InterpreterContext(
            ValueTable(classValue.classContext.fields),
            context.functions,
            context.methods,
            classValue.classContext,
            methodSignature.params.toMutableList(),
        )

        setParams(methodSignature.params, params, methodContext.fields, context)
        returns.forEach { methodContext.fields.create(it) }
        val body = classValue.classContext.methods.get(methodSignature.signature)
        interpretSequence(body, methodContext)
        return methodContext.fields
    }

    private fun interpretMethodCall(
        methodSignature: MethodSignatureAST,
        params: List<ExpressionAST>,
        returns: List<IdentifierAST>,
        context: InterpreterContext,
    ): ValueTable<IdentifierAST, Value> = if (methodSignature is ClassInstanceMethodSignatureAST) {
        interpretClassInstanceMethodCall(methodSignature, params, returns, context)
    } else {
        val (methodContext, body) = if (context.methods.has(methodSignature)) {
            Pair(
                InterpreterContext(ValueTable(), context.functions, context.methods, null, methodSignature.params),
                context.methods.get(methodSignature),
            )
        } else {
            Pair(
                InterpreterContext(
                    ValueTable(context.classContext!!.fields),
                    context.functions,
                    context.methods,
                    context.classContext,
                    methodSignature.params,
                ),
                context.classContext.methods.get(methodSignature),
            )
        }

        setParams(methodSignature.params, params, methodContext.fields, context)

        returns.forEach { methodContext.fields.create(it) }
        interpretSequence(body, methodContext)

        if (verify && returns.isNotEmpty()) {
            val returnCondition = returns
                .map { Pair(it, interpretIdentifier(it, methodContext).toExpressionAST()) }
                .map { BinaryExpressionAST(it.first, EqualsOperator, it.second) }
                .conjunct()

            val ensuresCondition = if (params.isEmpty()) {
                returnCondition
            } else {
                val paramCondition = methodSignature.params
                    .map { Pair(it, interpretIdentifier(it, methodContext).toExpressionAST()) }
                    .map { BinaryExpressionAST(it.first, EqualsOperator, it.second) }
                    .conjunct()
                BinaryExpressionAST(paramCondition, ImplicationOperator, returnCondition)
            }

            methodSignature.annotations.add(EnsuresAnnotation(ensuresCondition))
        }

        methodContext.fields
    }

    override fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST, context: InterpreterContext): Value {
        val methodFields = interpretMethodCall(methodCall.method, methodCall.params, methodCall.method.returns, context)
        return MultiValue(methodCall.method.returns.map { r -> methodFields.get(r) })
    }

    override fun interpretClassInstantiation(
        classInstantiation: ClassInstantiationAST,
        context: InterpreterContext,
    ): Value {
        val classFields = classInstantiation.clazz.constructorFields
        val constructorParams = classInstantiation.params.map { interpretExpression(it, context) }
        val classContext = InterpreterContext()

        classFields.zip(constructorParams).forEach { classContext.fields.declare(it.first, it.second) }
        classInstantiation.clazz.functionMethods.forEach { classContext.functions.declare(it.signature, it.getBody()) }
        classInstantiation.clazz.methods.forEach { classContext.methods.declare(it.signature, it.getBody()) }

        return ClassValue(classContext)
    }

    override fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST, context: InterpreterContext): Value =
        if (binaryExpression.operator == ConjunctionOperator) {
            // short circuit evaluation
            val lhs = interpretExpression(binaryExpression.expr1, context)
            (lhs as BoolValue).shortAnd { interpretExpression(binaryExpression.expr2, context) as BoolValue }
        } else if (binaryExpression.operator == DisjunctionOperator) {
            val lhs = interpretExpression(binaryExpression.expr1, context)
            (lhs as BoolValue).shortOr { interpretExpression(binaryExpression.expr2, context) as BoolValue }
        } else {
            val lhs = interpretExpression(binaryExpression.expr1, context)
            val rhs = interpretExpression(binaryExpression.expr2, context)

            when (binaryExpression.operator) {
                IffOperator -> (lhs as BoolValue).iff(rhs as BoolValue)
                ImplicationOperator -> (lhs as BoolValue).impl(rhs as BoolValue)
                ReverseImplicationOperator -> (lhs as BoolValue).rimpl(rhs as BoolValue)
                LessThanOperator -> (lhs as IntValue).lessThan(rhs as IntValue)
                LessThanEqualOperator -> (lhs as IntValue).lessThanEquals(rhs as IntValue)
                GreaterThanEqualOperator -> (lhs as IntValue).greaterThanEquals(rhs as IntValue)
                GreaterThanOperator -> (lhs as IntValue).greaterThan(rhs as IntValue)
                EqualsOperator, DataStructureEqualityOperator -> BoolValue(lhs == rhs)
                NotEqualsOperator, DataStructureInequalityOperator -> BoolValue(lhs != rhs)
                AdditionOperator -> (lhs as IntValue).plus(rhs as IntValue)
                SubtractionOperator -> (lhs as IntValue).subtract(rhs as IntValue)
                MultiplicationOperator -> (lhs as IntValue).multiply(rhs as IntValue)
                DivisionOperator -> (lhs as IntValue).divide(rhs as IntValue)
                ModuloOperator -> (lhs as IntValue).modulo(rhs as IntValue)
                MembershipOperator -> (rhs as DataStructureValue).contains(lhs)
                AntiMembershipOperator -> (rhs as DataStructureValue).notContains(lhs)
                ProperSubsetOperator -> interpretProperSubset(lhs, rhs)
                SubsetOperator -> interpretSubset(lhs, rhs)
                SupersetOperator -> interpretSuperset(lhs, rhs)
                ProperSupersetOperator -> interpretProperSuperset(lhs, rhs)
                DisjointOperator -> interpretDisjoint(lhs, rhs)
                UnionOperator -> interpretUnion(lhs, rhs)
                DifferenceOperator -> interpretDifference(lhs, rhs)
                IntersectionOperator -> interpretIntersection(lhs, rhs)
                else -> throw UnsupportedOperationException()
            }
        }

    private fun interpretProperSubset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.properSubsetOf(rhs as MultisetValue)
        is SetValue -> lhs.properSubsetOf(rhs as SetValue)
        is SequenceValue -> lhs.properSubsetOf(rhs as SequenceValue)
        is StringValue -> interpretSubset(lhs.toSequence(), if (rhs is StringValue) rhs.toSequence() else rhs)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretSubset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.subsetOf(rhs as MultisetValue)
        is SetValue -> lhs.subsetOf(rhs as SetValue)
        is SequenceValue -> lhs.subsetOf(rhs as SequenceValue)
        is StringValue -> interpretSubset(lhs.toSequence(), if (rhs is StringValue) rhs.toSequence() else rhs)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretSuperset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.supersetOf(rhs as MultisetValue)
        is SetValue -> lhs.supersetOf(rhs as SetValue)
        is SequenceValue -> lhs.supersetOf(rhs as SequenceValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretProperSuperset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.properSupersetOf(rhs as MultisetValue)
        is SetValue -> lhs.properSupersetOf(rhs as SetValue)
        is SequenceValue -> lhs.properSupersetOf(rhs as SequenceValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretDisjoint(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.disjoint(rhs as MultisetValue)
        is SetValue -> lhs.disjoint(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretUnion(lhs: Value, rhs: Value): Value {
        return when (lhs) {
            is MultisetValue -> lhs.union(rhs as MultisetValue)
            is SetValue -> lhs.union(rhs as SetValue)
            is MapValue -> lhs.union(rhs as MapValue)
            is StringValue -> {
                val rhsAsStringValue = (if (rhs is SequenceValue) rhs.asStringValue() else rhs) as StringValue
                lhs.concat(rhsAsStringValue)
            }

            is SequenceValue -> {
                if (rhs is StringValue) {
                    val lhsAsStringValue = lhs.asStringValue()
                    lhsAsStringValue.concat(rhs)
                } else {
                    lhs.union(rhs as SequenceValue)
                }
            }

            else -> throw UnsupportedOperationException()
        }
    }

    private fun interpretDifference(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.difference(rhs as MultisetValue)
        is SetValue -> lhs.difference(rhs as SetValue)
        is MapValue -> lhs.difference(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretIntersection(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.intersect(rhs as MultisetValue)
        is SetValue -> lhs.intersect(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    override fun interpretTernaryExpression(
        ternaryExpression: TernaryExpressionAST,
        context: InterpreterContext,
    ): Value {
        val condition = interpretExpression(ternaryExpression.condition, context)
        return if ((condition as BoolValue).value) {
            interpretExpression(ternaryExpression.ifBranch, context)
        } else {
            interpretExpression(ternaryExpression.elseBranch, context)
        }
    }

    override fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST, context: InterpreterContext): Value {
        val exprValue = interpretExpression(unaryExpression.expr, context)
        return if (unaryExpression.operator == NegationOperator) {
            (exprValue as IntValue).negate()
        } else {
            (exprValue as BoolValue).not()
        }
    }

    override fun interpretModulus(modulus: ModulusExpressionAST, context: InterpreterContext): Value =
        (interpretExpression(modulus.expr, context) as DataStructureValue).modulus()

    override fun interpretMultisetConversion(
        multisetConversion: MultisetConversionAST,
        context: InterpreterContext,
    ): Value {
        val value = interpretExpression(multisetConversion.expr, context)
        val sequenceValues =
            if (value is SequenceValue) value.seq else (value as StringValue).value.map { CharValue(it) }
        return MultisetValue(multisetConversion.type().innerType, sequenceValues.toMultiset())
    }

    override fun interpretIdentifier(identifier: IdentifierAST, context: InterpreterContext): Value =
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, context) as ClassValue
                interpretIdentifier(
                    identifier.classField,
                    InterpreterContext(context.fields, context.functions, context.methods, classValue.classContext),
                )
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, context) as ArrayValue
                val index = interpretExpression(identifier.index, context) as IntValue
                arrayValue.getIndex(index.value.toInt())
            }

            is DatatypeDestructorAST -> interpretDatatypeDestructor(identifier, context)

            else -> if (context.fields.has(identifier)) {
                context.fields.get(identifier)
            } else {
                context.classContext!!.fields.get(identifier)
            }
        }

    override fun interpretIndex(index: IndexAST, context: InterpreterContext): Value = when (index) {
        is SequenceIndexAST -> {
            val value = interpretExpression(index.sequence, context)
            val key = interpretExpression(index.key, context) as IntValue
            if (value is SequenceValue) {
                value.getIndex(key.value.toInt())
            } else {
                (value as StringValue).getIndex(key.value.toInt())
            }
        }

        is MapIndexAST -> {
            val key = interpretExpression(index.key, context)
            val mapValue = interpretExpression(index.map, context) as MapValue
            mapValue.get(key)
        }

        is MultisetIndexAST -> {
            val key = interpretExpression(index.key, context)
            val multisetValue = interpretExpression(index.multiset, context) as MultisetValue
            multisetValue.get(key)
        }

        else -> throw UnsupportedOperationException()
    }

    private fun interpretIndexAssign(indexAssign: IndexAssignAST, context: InterpreterContext): Value {
        val key = interpretExpression(indexAssign.key, context)
        val value = interpretExpression(indexAssign.value, context)
        return when (val ident = interpretExpression(indexAssign.expression, context)) {
            is MultisetValue -> ident.assign(key, (value as IntValue).value.toInt())
            is MapValue -> ident.assign(key, value)
            is SequenceValue -> ident.assign((key as IntValue).value.toInt(), value)
            is StringValue -> ident.assign((key as IntValue).value.toInt(), value as CharValue)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun interpretSetDisplay(setDisplay: SetDisplayAST, context: InterpreterContext): Value {
        val values = setDisplay.exprs.map { interpretExpression(it, context) }
        return if (setDisplay.isMultiset) MultisetValue(setDisplay.innerType, values.toMultiset()) else SetValue(setDisplay.innerType, values.toSet())
    }

    override fun interpretSetComprehension(setComprehension: SetComprehensionAST, context: InterpreterContext): Value =
        when (setComprehension) {
            is IntRangeSetComprehensionAST -> interpretIntRangeSetComprehension(setComprehension, context)
            is DataStructureSetComprehensionAST -> interpretDataStructureSetComprehension(setComprehension, context)
            else -> throw UnsupportedOperationException()
        }

    private fun interpretIntRangeSetComprehension(setComprehension: IntRangeSetComprehensionAST, context: InterpreterContext): Value {
        val set = mutableSetOf<Value>()

        val bottomRange = interpretExpression(setComprehension.bottomRange, context) as IntValue
        val topRange = interpretExpression(setComprehension.topRange, context) as IntValue

        var i = bottomRange.value
        while (i < topRange.value) {
            val exprContext = context.increaseDepth()
            exprContext.fields.declare(setComprehension.identifier, IntValue(i))
            set.add(interpretExpression(setComprehension.expr, exprContext))
            i += ONE
        }

        return SetValue(setComprehension.type().innerType, set)
    }

    private fun interpretDataStructureSetComprehension(setComprehension: DataStructureSetComprehensionAST, context: InterpreterContext): Value {
        val set = mutableSetOf<Value>()
        val dataStructure = interpretExpression(setComprehension.dataStructure, context) as DataStructureValue

        dataStructure.elements().forEach { elemValue ->
            val exprContext = context.increaseDepth()
            exprContext.fields.declare(setComprehension.identifier, elemValue)
            set.add(interpretExpression(setComprehension.expr, exprContext))
        }

        return SetValue(setComprehension.type().innerType, set)
    }

    override fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST, context: InterpreterContext): Value =
        SequenceValue(sequenceDisplay.exprs.map { interpretExpression(it, context) })

    override fun interpretSequenceComprehension(sequenceComprehension: SequenceComprehensionAST, context: InterpreterContext): Value {
        val size = interpretExpression(sequenceComprehension.size, context) as IntValue

        val exprContext = context.increaseDepth()
        val exprValues = mutableListOf<Value>()
        var i = ZERO
        while (i < size.value) {
            exprContext.fields.declare(sequenceComprehension.identifier, IntValue(i))
            exprValues.add(interpretExpression(sequenceComprehension.expr, exprContext))
            i += ONE
        }

        return SequenceValue(exprValues)
    }

    override fun interpretMapConstructor(mapConstructor: MapConstructorAST, context: InterpreterContext): Value {
        val map = mutableMapOf<Value, Value>()
        mapConstructor.assignments.forEach { (k, v) ->
            val key = interpretExpression(k, context)
            val value = interpretExpression(v, context)
            map[key] = value
        }
        return MapValue(mapConstructor.type(), map)
    }

    override fun interpretMapComprehension(mapComprehension: MapComprehensionAST, context: InterpreterContext): Value =
        when (mapComprehension) {
            is IntRangeMapComprehensionAST -> interpretIntRangeMapComprehension(mapComprehension, context)
            is DataStructureMapComprehensionAST -> interpretDataStructureMapComprehension(mapComprehension, context)
            else -> throw UnsupportedOperationException()
        }

    private fun interpretIntRangeMapComprehension(mapComprehension: IntRangeMapComprehensionAST, context: InterpreterContext): Value {
        val map = mutableMapOf<Value, Value>()

        val bottomRange = interpretExpression(mapComprehension.bottomRange, context) as IntValue
        val topRange = interpretExpression(mapComprehension.topRange, context) as IntValue

        var i = bottomRange.value
        while (i < topRange.value) {
            val exprContext = context.increaseDepth()
            exprContext.fields.declare(mapComprehension.identifier, IntValue(i))

            val key = interpretExpression(mapComprehension.assign.first, exprContext)
            val value = interpretExpression(mapComprehension.assign.second, exprContext)

            map[key] = value

            i += ONE
        }

        return MapValue(mapComprehension.type(), map)
    }

    private fun interpretDataStructureMapComprehension(mapComprehension: DataStructureMapComprehensionAST, context: InterpreterContext): Value {
        val map = mutableMapOf<Value, Value>()
        val dataStructureValue = interpretExpression(mapComprehension.dataStructure, context) as DataStructureValue

        dataStructureValue.elements().forEach { elemValue ->
            val exprContext = context.increaseDepth()
            exprContext.fields.declare(mapComprehension.identifier, elemValue)

            val key = interpretExpression(mapComprehension.assign.first, exprContext)
            val value = interpretExpression(mapComprehension.assign.second, exprContext)

            map[key] = value
        }

        return MapValue(mapComprehension.type(), map)
    }

    override fun interpretArrayLength(arrayLength: ArrayLengthAST, context: InterpreterContext): Value {
        val array = interpretIdentifier(arrayLength.array, context) as ArrayValue
        return array.length()
    }

    override fun interpretArrayInit(arrayInit: ArrayInitAST, context: InterpreterContext): Value =
        when (arrayInit) {
            is ComprehensionInitialisedArrayInitAST -> interpretComprehensionInitialisedArrayInit(arrayInit, context)
            is ValueInitialisedArrayInitAST -> interpretValueInitialisedArrayInit(arrayInit, context)
            else -> ArrayValue(arrayInit.length)
        }

    private fun interpretComprehensionInitialisedArrayInit(arrayInit: ComprehensionInitialisedArrayInitAST, context: InterpreterContext): Value {
        val array = ArrayValue(arrayInit.length)

        (0 until arrayInit.length).forEach { i ->
            val exprContext = context.increaseDepth()
            exprContext.fields.declare(arrayInit.identifier, IntValue(valueOf(i.toLong())))
            array.setIndex(i, interpretExpression(arrayInit.expr, exprContext))
        }

        return array
    }

    private fun interpretValueInitialisedArrayInit(arrayInit: ValueInitialisedArrayInitAST, context: InterpreterContext): Value {
        val array = ArrayValue(arrayInit.length)
        val values = arrayInit.values.map { interpretExpression(it, context) }

        values.indices.forEach { i ->
            array.setIndex(i, values[i])
        }

        return array
    }

    override fun interpretCharacterLiteral(
        characterLiteral: CharacterLiteralAST,
        context: InterpreterContext,
    ): CharValue =
        CharValue(characterLiteral.value)

    override fun interpretStringLiteral(stringLiteral: StringLiteralAST, context: InterpreterContext): StringValue =
        StringValue(stringLiteral.value)

    override fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST, context: InterpreterContext): IntValue =
        IntValue(valueOf(intLiteral.value.toLong()))

    override fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST, context: InterpreterContext): BoolValue =
        BoolValue(boolLiteral.value)
}
