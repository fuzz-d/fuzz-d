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
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
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
import fuzzd.generator.ast.MethodSignatureAST
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
import fuzzd.generator.ast.Type.MapType
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
import fuzzd.interpreter.value.Value.ClassValue
import fuzzd.interpreter.value.Value.DataStructureValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.MapValue
import fuzzd.interpreter.value.Value.MultiValue
import fuzzd.interpreter.value.Value.MultisetValue
import fuzzd.interpreter.value.Value.SequenceValue
import fuzzd.interpreter.value.Value.SetValue
import fuzzd.interpreter.value.Value.StringValue
import fuzzd.interpreter.value.ValueTable
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.ADVANCED_ABSOLUTE
import fuzzd.utils.ADVANCED_SAFE_ARRAY_INDEX
import fuzzd.utils.ADVANCED_SAFE_DIV_INT
import fuzzd.utils.ADVANCED_SAFE_MODULO_INT
import fuzzd.utils.SAFE_ARRAY_INDEX
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.reduceLists
import fuzzd.utils.toMultiset
import java.math.BigInteger

class Interpreter : ASTInterpreter {
    private val output = StringBuilder()
    private var doBreak = false

    /* ============================== TOP LEVEL ============================== */
    override fun interpretDafny(dafny: DafnyAST): Pair<String, List<StatementAST>> {
        val context = InterpreterContext()
        dafny.topLevelElements.filterIsInstance<MethodAST>().forEach { method ->
            context.methods.assign(method.signature, method.getBody())
        }

        dafny.topLevelElements.filterIsInstance<FunctionMethodAST>().forEach { function ->
            context.functions.assign(function.signature, function.body)
        }

        listOf(
            ADVANCED_ABSOLUTE,
            ADVANCED_SAFE_ARRAY_INDEX,
            ADVANCED_SAFE_DIV_INT,
            ADVANCED_SAFE_MODULO_INT
        ).forEach { context.methods.assign(it.signature, it.getBody()) }

        listOf(
            ABSOLUTE,
            SAFE_ARRAY_INDEX,
            SAFE_DIVISION_INT,
            SAFE_MODULO_INT
        ).forEach { context.functions.assign(it.signature, it.body) }

        val mainFunction = dafny.topLevelElements.first { it is MainFunctionAST }
        val prints = interpretMainFunction(mainFunction as MainFunctionAST, context)
        return Pair(output.toString(), prints)
    }

    override fun interpretMainFunction(mainFunction: MainFunctionAST, context: InterpreterContext): List<StatementAST> {
        interpretSequence(mainFunction.sequenceAST, context)

        // generate checksum prints
        val prints = context.fields.values
            .filter { (_, v) -> v != null }
            .map { (k, v) ->
                generateChecksumPrint(k, v!!, context)
            }.reduceLists()
        prints.forEach { interpretPrint(it, context) }

        return prints
    }

    private fun generateChecksumPrint(key: IdentifierAST, value: Value, context: InterpreterContext): List<PrintAST> =
        when (value) {
            is MultiValue, is StringValue, is IntValue, is BoolValue -> listOf(PrintAST(key))
            is SetValue, is MultisetValue, is MapValue, is SequenceValue -> {
                if (key.type().hasArrayType()) {
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
                val classInstance = key as ClassInstanceAST
                classInstance.fields.map { generateChecksumPrint(it, interpretIdentifier(it, context), context) }
                    .reduceLists()
            }
        }

    override fun interpretSequence(sequence: SequenceAST, context: InterpreterContext) {
        sequence.statements.forEach { interpretStatement(it, context) }
    }

    /* ============================== STATEMENTS ============================= */

    override fun interpretStatement(statement: StatementAST, context: InterpreterContext) {
        if (doBreak) return

        return when (statement) {
            is BreakAST -> {
                doBreak = true
            }

            is IfStatementAST -> interpretIfStatement(statement, context)
            is CounterLimitedWhileLoopAST -> interpretCounterLimitedWhileStatement(statement, context)
            is WhileLoopAST -> interpretWhileStatement(statement, context)
            is VoidMethodCallAST -> interpretVoidMethodCall(statement, context)
            is MultiTypedDeclarationAST -> interpretMultiTypedDeclaration(statement, context)
            is MultiDeclarationAST -> interpretMultiDeclaration(statement, context)
            is MultiAssignmentAST -> interpretMultiAssign(statement, context)
            is PrintAST -> interpretPrint(statement, context)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun interpretIfStatement(ifStatement: IfStatementAST, context: InterpreterContext) {
        val conditionValue = interpretExpression(ifStatement.condition, context) as BoolValue

        if (conditionValue.value) {
            interpretSequence(ifStatement.ifBranch, context.increaseDepth())
        } else {
            ifStatement.elseBranch?.let { interpretSequence(it, context.increaseDepth()) }
        }
    }

    override fun interpretCounterLimitedWhileStatement(
        whileStatement: CounterLimitedWhileLoopAST, context: InterpreterContext
    ) {
        interpretStatement(whileStatement.counterInitialisation, context)
        var condition = interpretExpression(whileStatement.condition, context)
        val prevBreak = doBreak
        doBreak = false
        while ((condition as BoolValue).value && !doBreak) {
            val innerContext = context.increaseDepth()
            interpretStatement(whileStatement.terminationCheck, innerContext)
            if (doBreak) {
                break;
            }
            interpretStatement(whileStatement.counterUpdate, innerContext)
            interpretSequence(whileStatement.body, innerContext)
            condition = interpretExpression(whileStatement.condition, context)
        }
        doBreak = prevBreak
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
        typedDeclaration: MultiTypedDeclarationAST, context: InterpreterContext
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
        isDeclaration: Boolean
    ) {
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, context) as ClassValue
                setIdentifierValue(identifier.classField, value, context.classField(classValue.fields), isDeclaration)
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, context) as ArrayValue
                val index = interpretExpression(identifier.index, context) as IntValue
                arrayValue.setIndex(index.value.toInt(), value)
            }

            is SequenceIndexAST, is IndexAST, is IndexAssignAST -> throw UnsupportedOperationException()

            else -> if (isDeclaration) {
                context.fields.declare(identifier, value)
            } else {
                context.fields.assign(identifier, value)
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
            is ClassValue -> emitClassValue(value)
            is ArrayValue -> emitArrayValue(value)
            is SetValue -> emitSetValue(value)
            is MultisetValue -> emitMultisetValue(value)
            is MultiValue -> emitMultiValue(value)
            is MapValue -> emitMapValue(value)
            is SequenceValue -> emitSequenceValue(value)
            is StringValue -> emitStringValue(value)
            is IntValue -> emitIntValue(value)
            is BoolValue -> emitBoolValue(value)
        }
    }

    private fun emitClassValue(classValue: ClassValue) {
        classValue.fields.values
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
        sequenceValue.seq.forEach { emitOutput(it) }
    }

    private fun emitStringValue(stringValue: StringValue) {
        output.append(stringValue.value)
    }

    private fun emitIntValue(intValue: IntValue) {
        output.append(intValue.value)
    }

    private fun emitBoolValue(boolValue: BoolValue) {
        output.append(boolValue.value)
    }

    /* ============================== EXPRESSIONS ============================ */
    override fun interpretExpression(expression: ExpressionAST, context: InterpreterContext): Value =
        when (expression) {
            is FunctionMethodCallAST -> interpretFunctionMethodCall(expression, context)
            is NonVoidMethodCallAST -> interpretNonVoidMethodCall(expression, context)
            is ClassInstantiationAST -> interpretClassInstantiation(expression, context)
            is BinaryExpressionAST -> interpretBinaryExpression(expression, context)
            is TernaryExpressionAST -> interpretTernaryExpression(expression, context)
            is UnaryExpressionAST -> interpretUnaryExpression(expression, context)
            is ModulusExpressionAST -> interpretModulus(expression, context)
            is MultisetConversionAST -> interpretMultisetConversion(expression, context)
            is IdentifierAST -> interpretIdentifier(expression, context)
            is SetDisplayAST -> interpretSetDisplay(expression, context)
            is SequenceDisplayAST -> interpretSequenceDisplay(expression, context)
            is MapConstructorAST -> interpretMapConstructor(expression, context)
            is ArrayLengthAST -> interpretArrayLength(expression, context)
            is ArrayInitAST -> interpretArrayInit(expression, context)
            is StringLiteralAST -> interpretStringLiteral(expression, context)
            is IntegerLiteralAST -> interpretIntegerLiteral(expression, context)
            is BooleanLiteralAST -> interpretBooleanLiteral(expression, context)
            else -> throw UnsupportedOperationException()
        }

    override fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST, context: InterpreterContext): Value =
        if (functionCall.function is ClassInstanceFunctionMethodSignatureAST) {
            interpretClassInstanceFunctionMethodCall(functionCall, context)
        } else {
            val functionSignature = functionCall.function
            val functionParams = functionSignature.params

            val body = context.functions.get(functionSignature)
            val functionScopeValueTable = ValueTable<IdentifierAST, Value>()

            setParams(functionParams, functionCall.params, functionScopeValueTable, context)

            interpretExpression(body, context.functionCall(functionScopeValueTable))
        }


    private fun setParams(
        functionParams: List<IdentifierAST>,
        functionCallParams: List<ExpressionAST>,
        functionScopeValueTable: ValueTable<IdentifierAST, Value>,
        context: InterpreterContext
    ) {
        functionParams.indices.forEach { i ->
            functionScopeValueTable.assign(functionParams[i], interpretExpression(functionCallParams[i], context))
        }
    }

    private fun interpretClassInstanceFunctionMethodCall(
        functionCall: FunctionMethodCallAST, context: InterpreterContext
    ): Value {
        val functionSignature = functionCall.function as ClassInstanceFunctionMethodSignatureAST
        val functionParams = functionSignature.params

        val classValue = interpretIdentifier(functionSignature.classInstance, context) as ClassValue
        val body = classValue.functions.getValue(functionSignature.signature)

        val functionContext = context.functionCall(classValue)
        setParams(functionParams, functionCall.params, functionContext.fields, context)

        val expr = interpretExpression(body, functionContext)

        return expr
    }

    private fun interpretMethodCall(
        methodSignature: MethodSignatureAST,
        params: List<ExpressionAST>,
        returns: List<IdentifierAST>,
        context: InterpreterContext,
    ): ValueTable<IdentifierAST, Value> = if (methodSignature is ClassInstanceMethodSignatureAST) {
        interpretClassInstanceMethodCall(methodSignature, params, returns, context)
    } else {
        val methodScopeValueTable =
            if (context.fields.topLevel() == context.fields) ValueTable() else ValueTable(context.fields)
        val body = context.methods.get(methodSignature)
        val methodParams = methodSignature.params

        setParams(methodParams, params, methodScopeValueTable, context)
        returns.forEach { methodScopeValueTable.create(it) }

        interpretSequence(body, context.functionCall(methodScopeValueTable))
        methodScopeValueTable
    }

    private fun interpretClassInstanceMethodCall(
        methodSignature: ClassInstanceMethodSignatureAST,
        params: List<ExpressionAST>,
        returns: List<IdentifierAST>,
        context: InterpreterContext
    ): ValueTable<IdentifierAST, Value> {
        val classValue = interpretIdentifier(methodSignature.classInstance, context) as ClassValue
        val body = classValue.methods.getValue(methodSignature.signature)
        val methodContext = context.functionCall(classValue)

        setParams(methodSignature.params, params, methodContext.fields, context)
        returns.forEach { methodContext.fields.create(it) }

        interpretSequence(body, methodContext)
        return methodContext.fields
    }

    override fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST, context: InterpreterContext): Value {
        val methodScopeValueTable =
            interpretMethodCall(methodCall.method, methodCall.params, methodCall.method.returns, context)
        return MultiValue(methodCall.method.returns.map { r -> methodScopeValueTable.get(r) })
    }

    override fun interpretClassInstantiation(
        classInstantiation: ClassInstantiationAST, context: InterpreterContext
    ): Value {
        val classFields = classInstantiation.clazz.constructorFields
        val constructorParams = classInstantiation.params.map { interpretExpression(it, context) }
        val classValueTable = ValueTable<IdentifierAST, Value>()
        classFields.zip(constructorParams).forEach { classValueTable.assign(it.first, it.second) }

        val methods = classInstantiation.clazz.methods.associate { Pair(it.signature, it.getBody()) }
        val functions = classInstantiation.clazz.functionMethods.associate { Pair(it.signature, it.body) }

        return ClassValue(classValueTable, functions, methods)
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
        else -> throw UnsupportedOperationException()
    }

    private fun interpretSubset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.subsetOf(rhs as MultisetValue)
        is SetValue -> lhs.subsetOf(rhs as SetValue)
        is SequenceValue -> lhs.subsetOf(rhs as SequenceValue)
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

    private fun interpretUnion(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.union(rhs as MultisetValue)
        is SetValue -> lhs.union(rhs as SetValue)
        is MapValue -> lhs.union(rhs as MapValue)
        is SequenceValue -> lhs.union(rhs as SequenceValue)
        else -> throw UnsupportedOperationException()
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
        ternaryExpression: TernaryExpressionAST, context: InterpreterContext
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
        multisetConversion: MultisetConversionAST, context: InterpreterContext
    ): Value {
        val sequenceValue = interpretExpression(multisetConversion.expr, context) as SequenceValue
        return MultisetValue(sequenceValue.seq.toMultiset())
    }

    override fun interpretIdentifier(identifier: IdentifierAST, context: InterpreterContext): Value =
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, context) as ClassValue
                interpretIdentifier(identifier.classField, context.classField(classValue.fields))
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, context) as ArrayValue
                val index = interpretExpression(identifier.index, context) as IntValue

                arrayValue.getIndex(index.value.toInt())
            }

            is SequenceIndexAST -> {
                val sequenceValue = interpretIdentifier(identifier.sequence, context) as SequenceValue
                val index = interpretExpression(identifier.index, context) as IntValue

                sequenceValue.getIndex(index.value.toInt())
            }

            is IndexAST -> {
                val key = interpretExpression(identifier.key, context)
                if (identifier.ident.type() is MapType) {
                    val mapValue = interpretIdentifier(identifier.ident, context) as MapValue
                    mapValue.get(key)
                } else {
                    val multisetValue = interpretIdentifier(identifier.ident, context) as MultisetValue
                    multisetValue.get(key)
                }
            }

            is IndexAssignAST -> interpretIndexAssign(identifier, context)

            else -> context.fields.get(identifier)
        }

    private fun interpretIndexAssign(indexAssign: IndexAssignAST, context: InterpreterContext): Value {
        val key = interpretExpression(indexAssign.key, context)
        val value = interpretExpression(indexAssign.value, context)

        return when (val ident = interpretExpression(indexAssign.ident, context)) {
            is MultisetValue -> ident.assign(key, (value as IntValue).value.toInt())
            is MapValue -> ident.assign(key, value)
            is SequenceValue -> ident.assign((key as IntValue).value.toInt(), value)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun interpretSetDisplay(setDisplay: SetDisplayAST, context: InterpreterContext): Value {
        val values = setDisplay.exprs.map { interpretExpression(it, context) }
        return if (setDisplay.isMultiset) MultisetValue(values.toMultiset()) else SetValue(values.toSet())
    }

    override fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST, context: InterpreterContext): Value =
        SequenceValue(sequenceDisplay.exprs.map { interpretExpression(it, context) })

    override fun interpretMapConstructor(mapConstructor: MapConstructorAST, context: InterpreterContext): Value {
        val map = mutableMapOf<Value, Value>()
        mapConstructor.assignments.forEach { (k, v) ->
            val key = interpretExpression(k, context)
            val value = interpretExpression(v, context)
            map[key] = value
        }
        return MapValue(map)
    }

    override fun interpretArrayLength(arrayLength: ArrayLengthAST, context: InterpreterContext): Value {
        val array = interpretIdentifier(arrayLength.array, context) as ArrayValue
        return array.length()
    }

    override fun interpretArrayInit(arrayInit: ArrayInitAST, context: InterpreterContext): Value =
        ArrayValue(arrayInit.length)

    override fun interpretStringLiteral(stringLiteral: StringLiteralAST, context: InterpreterContext): StringValue =
        StringValue(stringLiteral.value)

    override fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST, context: InterpreterContext): IntValue =
        IntValue(BigInteger.valueOf(intLiteral.value.toLong()))

    override fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST, context: InterpreterContext): BoolValue =
        BoolValue(boolLiteral.value)
}
