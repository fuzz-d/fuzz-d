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
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.BreakAST
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

class Interpreter : ASTInterpreter {
    private val output = StringBuilder()
    private val topLevelMethods = mutableMapOf<MethodSignatureAST, SequenceAST>()
    private val topLevelFunctions = mutableMapOf<FunctionMethodSignatureAST, ExpressionAST>()
    private var doBreak = false

    /* ============================== TOP LEVEL ============================== */
    override fun interpretDafny(dafny: DafnyAST): Pair<String, List<StatementAST>> {
        dafny.topLevelElements.filterIsInstance<MethodAST>().forEach { method ->
            topLevelMethods[method.signature] = method.getBody()
        }

        dafny.topLevelElements.filterIsInstance<FunctionMethodAST>().forEach { function ->
            topLevelFunctions[function.signature] = function.body
        }

        listOf(ADVANCED_ABSOLUTE, ADVANCED_SAFE_ARRAY_INDEX, ADVANCED_SAFE_DIV_INT, ADVANCED_SAFE_MODULO_INT)
            .forEach { topLevelMethods[it.signature] = it.getBody() }

        listOf(ABSOLUTE, SAFE_ARRAY_INDEX, SAFE_DIVISION_INT, SAFE_MODULO_INT)
            .forEach { topLevelFunctions[it.signature] = it.body }

        val mainFunction = dafny.topLevelElements.first { it is MainFunctionAST }
        val prints = interpretMainFunction(mainFunction as MainFunctionAST)
        return Pair(output.toString(), prints)
    }

    override fun interpretMainFunction(mainFunction: MainFunctionAST): List<StatementAST> {
        val valueTable = ValueTable()
        interpretSequence(mainFunction.sequenceAST, valueTable)

        // generate checksum prints
        val prints = valueTable.values.map { (k, v) ->
            generateChecksumPrint(k, v, valueTable)
        }.reduceLists()
        prints.forEach { interpretPrint(it, valueTable) }

        return prints
    }

    private fun generateChecksumPrint(key: IdentifierAST, value: Value, valueTable: ValueTable): List<PrintAST> =
        when (value) {
            is MultiValue -> listOf(PrintAST(value.toExpressionAST()))
            is StringValue, is IntValue, is BoolValue -> listOf(PrintAST(key))
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
                    generateChecksumPrint(identifier, interpretIdentifier(identifier, valueTable), valueTable)
                }.reduceLists()
            }

            is ClassValue -> {
                val classInstance = key as ClassInstanceAST
                classInstance.fields
                    .map { generateChecksumPrint(it, interpretIdentifier(it, valueTable), valueTable) }
                    .reduceLists()
            }
        }

    override fun interpretSequence(sequence: SequenceAST, valueTable: ValueTable) {
        sequence.statements.forEach { interpretStatement(it, valueTable) }
    }

    /* ============================== STATEMENTS ============================= */

    override fun interpretStatement(statement: StatementAST, valueTable: ValueTable) = when (statement) {
        is BreakAST -> {
            doBreak = true
        }

        is IfStatementAST -> interpretIfStatement(statement, valueTable)
        is WhileLoopAST -> interpretWhileStatement(statement, valueTable)
        is VoidMethodCallAST -> interpretVoidMethodCall(statement, valueTable)
        is MultiTypedDeclarationAST -> interpretMultiTypedDeclaration(statement, valueTable)
        is MultiDeclarationAST -> interpretMultiDeclaration(statement, valueTable)
        is MultiAssignmentAST -> interpretMultiAssign(statement, valueTable)
        is PrintAST -> interpretPrint(statement, valueTable)
        else -> throw UnsupportedOperationException()
    }

    override fun interpretIfStatement(ifStatement: IfStatementAST, valueTable: ValueTable) {
        val conditionValue = interpretExpression(ifStatement.condition, valueTable) as BoolValue

        if (conditionValue.value) {
            interpretSequence(ifStatement.ifBranch, ValueTable(valueTable))
        } else {
            ifStatement.elseBranch?.let { interpretSequence(it, ValueTable(valueTable)) }
        }
    }

    override fun interpretWhileStatement(whileStatement: WhileLoopAST, valueTable: ValueTable) {
        var condition = interpretExpression(whileStatement.condition, valueTable)
        val prevBreak = doBreak
        doBreak = false
        while ((condition as BoolValue).value && !doBreak) {
            interpretSequence(whileStatement.body, ValueTable(valueTable))
            condition = interpretExpression(whileStatement.condition, valueTable)
        }
        doBreak = prevBreak
    }

    override fun interpretVoidMethodCall(methodCall: VoidMethodCallAST, valueTable: ValueTable) {
        interpretMethodCall(methodCall.method, methodCall.params, valueTable)
    }

    override fun interpretMultiTypedDeclaration(typedDeclaration: MultiTypedDeclarationAST, valueTable: ValueTable) {
        val values = typedDeclaration.exprs.map { interpretExpression(it, valueTable) }
        typedDeclaration.identifiers.indices.forEach { i ->
            setIdentifierValue(typedDeclaration.identifiers[i], values[i], valueTable)
        }
    }

    override fun interpretMultiDeclaration(declaration: MultiDeclarationAST, valueTable: ValueTable) {
        if (declaration.exprs.size == 1 && declaration.identifiers.size > 1) {
            // non void method call
            val methodReturns = interpretExpression(declaration.exprs[0], valueTable) as MultiValue
            declaration.identifiers.indices.forEach { i ->
                setIdentifierValue(declaration.identifiers[i], methodReturns.values[i], valueTable)
            }
        } else {
            val values = declaration.exprs.map { interpretExpression(it, valueTable) }
            declaration.identifiers.indices.forEach { i ->
                setIdentifierValue(declaration.identifiers[i], values[i], valueTable)
            }
        }
    }

    override fun interpretMultiAssign(assign: MultiAssignmentAST, valueTable: ValueTable) {
        val values = assign.exprs.map { interpretExpression(it, valueTable) }
        assign.identifiers.indices.forEach { i ->
            setIdentifierValue(assign.identifiers[i], values[i], valueTable)
        }
    }

    private fun setIdentifierValue(identifier: IdentifierAST, value: Value, valueTable: ValueTable) {
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, valueTable) as ClassValue
                setIdentifierValue(identifier.classField, value, classValue.fields)
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, valueTable) as ArrayValue
                val index = interpretExpression(identifier.index, valueTable) as IntValue
                arrayValue.setIndex(index.value.toInt(), value)
            }

            is SequenceIndexAST, is IndexAST, is IndexAssignAST -> throw UnsupportedOperationException()

            else -> valueTable.set(identifier, value)
        }
    }

    override fun interpretPrint(printAST: PrintAST, valueTable: ValueTable) {
        val values = printAST.expr.map { interpretExpression(it, valueTable) }
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
            is MapValue -> emitMapValue(value)
            is SequenceValue -> emitSequenceValue(value)
            is StringValue -> emitStringValue(value)
            is IntValue -> emitIntValue(value)
            is BoolValue -> emitBoolValue(value)
            else -> throw UnsupportedOperationException()
        }
    }

    private fun emitClassValue(classValue: ClassValue) {
        classValue.fields.values.forEach { (_, v) ->
            emitOutput(v)
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
    override fun interpretExpression(expression: ExpressionAST, valueTable: ValueTable): Value =
        when (expression) {
            is FunctionMethodCallAST -> interpretFunctionMethodCall(expression, valueTable)
            is NonVoidMethodCallAST -> interpretNonVoidMethodCall(expression, valueTable)
            is ClassInstantiationAST -> interpretClassInstantiation(expression, valueTable)
            is BinaryExpressionAST -> interpretBinaryExpression(expression, valueTable)
            is TernaryExpressionAST -> interpretTernaryExpression(expression, valueTable)
            is UnaryExpressionAST -> interpretUnaryExpression(expression, valueTable)
            is ModulusExpressionAST -> interpretModulus(expression, valueTable)
            is MultisetConversionAST -> interpretMultisetConversion(expression, valueTable)
            is IdentifierAST -> interpretIdentifier(expression, valueTable)
            is SetDisplayAST -> interpretSetDisplay(expression, valueTable)
            is SequenceDisplayAST -> interpretSequenceDisplay(expression, valueTable)
            is MapConstructorAST -> interpretMapConstructor(expression, valueTable)
            is ArrayLengthAST -> interpretArrayLength(expression, valueTable)
            is ArrayInitAST -> interpretArrayInit(expression, valueTable)
            is StringLiteralAST -> interpretStringLiteral(expression, valueTable)
            is IntegerLiteralAST -> interpretIntegerLiteral(expression, valueTable)
            is BooleanLiteralAST -> interpretBooleanLiteral(expression, valueTable)
            else -> throw UnsupportedOperationException()
        }

    override fun interpretFunctionMethodCall(functionCall: FunctionMethodCallAST, valueTable: ValueTable): Value {
        val functionSignature = functionCall.function
        val functionParams = functionSignature.params

        val (body, functionScopeValueTable) = if (functionSignature is ClassInstanceFunctionMethodSignatureAST) {
            val classValue = interpretIdentifier(functionSignature.classInstance, valueTable) as ClassValue
            Pair(classValue.functions.getValue(functionSignature.signature), ValueTable(classValue.fields))
        } else {
            Pair(topLevelFunctions.getValue(functionSignature), ValueTable())
        }

        functionParams.indices.forEach { i ->
            functionScopeValueTable.set(functionParams[i], interpretExpression(functionCall.params[i], valueTable))
        }

        return interpretExpression(body, functionScopeValueTable)
    }

    private fun interpretMethodCall(
        methodSignature: MethodSignatureAST,
        params: List<ExpressionAST>,
        valueTable: ValueTable,
    ): ValueTable {
        val (body, methodScopeValueTable) = if (methodSignature is ClassInstanceMethodSignatureAST) {
            val classValue = interpretIdentifier(methodSignature.classInstance, valueTable) as ClassValue
            Pair(classValue.methods.getValue(methodSignature.signature), ValueTable(classValue.fields))
        } else {
            Pair(topLevelMethods.getValue(methodSignature), ValueTable())
        }

        val methodParams = methodSignature.params

        methodParams.indices.forEach { i ->
            methodScopeValueTable.set(methodParams[i], interpretExpression(params[i], valueTable))
        }
        interpretSequence(body, methodScopeValueTable)
        return methodScopeValueTable
    }

    override fun interpretNonVoidMethodCall(methodCall: NonVoidMethodCallAST, valueTable: ValueTable): Value {
        val methodScopeValueTable = interpretMethodCall(methodCall.method, methodCall.params, valueTable)
        return MultiValue(methodCall.method.returns.map { r -> methodScopeValueTable.get(r) })
    }

    override fun interpretClassInstantiation(classInstantiation: ClassInstantiationAST, valueTable: ValueTable): Value {
        val classFields = classInstantiation.clazz.constructorFields
        val constructorParams = classInstantiation.params.map { interpretExpression(it, valueTable) }
        val classValueTable = ValueTable()
        classFields.zip(constructorParams).forEach { classValueTable.set(it.first, it.second) }

        val methods = classInstantiation.clazz.methods.associate { Pair(it.signature, it.getBody()) }
        val functions = classInstantiation.clazz.functionMethods.associate { Pair(it.signature, it.body) }

        return ClassValue(classValueTable, functions, methods)
    }

    override fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST, valueTable: ValueTable): Value {
        val lhs = interpretExpression(binaryExpression.expr1, valueTable)
        val rhs = interpretExpression(binaryExpression.expr2, valueTable)

        return when (binaryExpression.operator) {
            IffOperator -> (lhs as BoolValue).iff(rhs as BoolValue)
            ImplicationOperator -> (lhs as BoolValue).impl(rhs as BoolValue)
            ReverseImplicationOperator -> (lhs as BoolValue).rimpl(rhs as BoolValue)
            ConjunctionOperator -> (lhs as BoolValue).and(rhs as BoolValue)
            DisjunctionOperator -> (lhs as BoolValue).or(rhs as BoolValue)
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

    override fun interpretTernaryExpression(ternaryExpression: TernaryExpressionAST, valueTable: ValueTable): Value {
        val condition = interpretExpression(ternaryExpression.condition, valueTable)
        return if ((condition as BoolValue).value) {
            interpretExpression(ternaryExpression.ifBranch, valueTable)
        } else {
            interpretExpression(ternaryExpression.elseBranch, valueTable)
        }
    }

    override fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST, valueTable: ValueTable): Value {
        val exprValue = interpretExpression(unaryExpression.expr, valueTable)
        return if (unaryExpression.operator == NegationOperator) {
            (exprValue as IntValue).negate()
        } else {
            (exprValue as BoolValue).not()
        }
    }

    override fun interpretModulus(modulus: ModulusExpressionAST, valueTable: ValueTable): Value =
        (interpretExpression(modulus.expr, valueTable) as DataStructureValue).modulus()

    override fun interpretMultisetConversion(multisetConversion: MultisetConversionAST, valueTable: ValueTable): Value {
        val sequenceValue = interpretExpression(multisetConversion.expr, valueTable) as SequenceValue
        return MultisetValue(sequenceValue.seq.toMultiset())
    }

    override fun interpretIdentifier(identifier: IdentifierAST, valueTable: ValueTable): Value =
        when (identifier) {
            is ClassInstanceFieldAST -> {
                val classValue = interpretIdentifier(identifier.classInstance, valueTable) as ClassValue
                interpretIdentifier(identifier.classField, classValue.fields)
            }

            is ArrayIndexAST -> {
                val arrayValue = interpretIdentifier(identifier.array, valueTable) as ArrayValue
                val index = interpretExpression(identifier.index, valueTable) as IntValue

                arrayValue.getIndex(index.value.toInt())
            }

            is SequenceIndexAST -> {
                val sequenceValue = interpretIdentifier(identifier.sequence, valueTable) as SequenceValue
                val index = interpretExpression(identifier.index, valueTable) as IntValue

                sequenceValue.getIndex(index.value.toInt())
            }

            is IndexAST -> {
                val key = interpretExpression(identifier.key, valueTable)
                if (identifier.ident.type() is MapType) {
                    val mapValue = interpretIdentifier(identifier.ident, valueTable) as MapValue
                    mapValue.get(key)
                } else {
                    val multisetValue = interpretIdentifier(identifier.ident, valueTable) as MultisetValue
                    multisetValue.get(key)
                }
            }

            is IndexAssignAST -> interpretIndexAssign(identifier, valueTable)

            else -> valueTable.get(identifier)
        }

    private fun interpretIndexAssign(indexAssign: IndexAssignAST, valueTable: ValueTable): Value {
        val key = interpretExpression(indexAssign.key, valueTable)
        val value = interpretExpression(indexAssign.value, valueTable)

        return when (val ident = interpretExpression(indexAssign.ident, valueTable)) {
            is MultisetValue -> ident.assign(key, (value as IntValue).value.toInt())
            is MapValue -> ident.assign(key, value)
            is SequenceValue -> ident.assign((key as IntValue).value.toInt(), value)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun interpretSetDisplay(setDisplay: SetDisplayAST, valueTable: ValueTable): Value {
        val values = setDisplay.exprs.map { interpretExpression(it, valueTable) }
        return if (setDisplay.isMultiset) MultisetValue(values.toMultiset()) else SetValue(values.toSet())
    }

    override fun interpretSequenceDisplay(sequenceDisplay: SequenceDisplayAST, valueTable: ValueTable): Value =
        SequenceValue(sequenceDisplay.exprs.map { interpretExpression(it, valueTable) })

    override fun interpretMapConstructor(mapConstructor: MapConstructorAST, valueTable: ValueTable): Value {
        val map = mutableMapOf<Value, Value>()
        mapConstructor.assignments.forEach { (k, v) ->
            val key = interpretExpression(k, valueTable)
            val value = interpretExpression(v, valueTable)
            map[key] = value
        }
        return MapValue(map)
    }

    override fun interpretArrayLength(arrayLength: ArrayLengthAST, valueTable: ValueTable): Value {
        val array = valueTable.get(arrayLength.array) as ArrayValue
        return array.length()
    }

    override fun interpretArrayInit(arrayInit: ArrayInitAST, valueTable: ValueTable): Value =
        ArrayValue(arrayInit.length)

    override fun interpretStringLiteral(stringLiteral: StringLiteralAST, valueTable: ValueTable): StringValue =
        StringValue(stringLiteral.value)

    override fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST, valueTable: ValueTable): IntValue =
        IntValue(intLiteral.value.toLong())

    override fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST, valueTable: ValueTable): BoolValue =
        BoolValue(boolLiteral.value)
}
