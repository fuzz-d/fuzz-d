package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIdentifierAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
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
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.error.IdentifierOnDemandException
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.error.MethodOnDemandException
import fuzzd.generator.ast.identifier_generator.NameGenerator.ClassNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.FieldNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.FunctionMethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.MethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ParameterNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ReturnsNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.TraitNameGenerator
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.ExpressionType
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.FUNCTION_METHOD_CALL
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.StatementType
import fuzzd.generator.selection.StatementType.ASSIGN
import fuzzd.generator.selection.StatementType.CLASS_INSTANTIATION
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.METHOD_CALL
import fuzzd.generator.selection.StatementType.PRINT
import fuzzd.generator.selection.StatementType.WHILE
import fuzzd.generator.symbol_table.GlobalSymbolTable
import fuzzd.generator.symbol_table.MethodCallTable
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_ADDITION_INT
import fuzzd.utils.SAFE_ADDITION_REAL
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_DIVISION_REAL
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.SAFE_MULTIPLY_INT
import fuzzd.utils.SAFE_MULTIPLY_REAL
import fuzzd.utils.SAFE_SUBTRACT_CHAR
import fuzzd.utils.SAFE_SUBTRACT_INT
import fuzzd.utils.SAFE_SUBTRACT_REAL
import fuzzd.utils.unionAll

class Generator(
    private val selectionManager: SelectionManager,
) : ASTGenerator {
    private val classNameGenerator = ClassNameGenerator()
    private val fieldNameGenerator = FieldNameGenerator()
    private val functionMethodNameGenerator = FunctionMethodNameGenerator()
    private val methodNameGenerator = MethodNameGenerator()
    private val traitNameGenerator = TraitNameGenerator()
    private val methodCallTable = MethodCallTable()

    /* ==================================== TOP LEVEL ==================================== */

    override fun generate(): TopLevelAST {
        val context = GenerationContext(GlobalSymbolTable())

        // init with safety function methods
        context.globalSymbolTable.addAllFunctionMethods(WRAPPER_FUNCTIONS)

        val mainFunction = generateMainFunction(context)
        val ast = mutableListOf<ASTElement>()

        context.globalSymbolTable.methods().forEach { method ->
            val functionContext = GenerationContext(context.globalSymbolTable, methodContext = method)
            method.params().forEach { param -> functionContext.symbolTable.add(param) }
            method.returns().forEach { r -> functionContext.symbolTable.add(r) }

            val body = generateMethodBody(functionContext, method)
            method.setBody(body)
        }

        ast.addAll(context.globalSymbolTable.functionMethods())
        ast.addAll(context.globalSymbolTable.methods())
        ast.addAll(context.globalSymbolTable.traits())
        ast.addAll(context.globalSymbolTable.classes())
        ast.add(mainFunction)

        return TopLevelAST(ast)
    }

    override fun generateMainFunction(context: GenerationContext): MainFunctionAST {
        val body = generateSequence(context)
        val printContext = context.disableOnDemand()
        val prints = (1..20).map { generatePrintStatement(printContext) }
        return MainFunctionAST(body.addStatements(prints))
    }

    override fun generateTrait(context: GenerationContext): TraitAST {
        val fields = (1..selectionManager.selectNumberOfFields()).map { generateField(context) }.toSet()

        val functionMethods =
            (1..selectionManager.selectNumberOfFunctionMethods()).map { generateFunctionMethodSignature(context) }
                .toSet()

        val methods = (1..selectionManager.selectNumberOfMethods()).map { generateMethodSignature(context) }.toSet()

        // TODO: extends map + extend traits

        return TraitAST(traitNameGenerator.newValue(), setOf() /* TODO() */, functionMethods, methods, fields)
    }

    override fun generateClass(context: GenerationContext): ClassAST {
        val classContext = GenerationContext(context.globalSymbolTable.clone())

        // get traits
        val numberOfTraits = selectionManager.selectNumberOfTraits()
        val traits = context.globalSymbolTable.traits().toMutableList()
        val selectedTraits = mutableSetOf<TraitAST>()

        for (i in 1..numberOfTraits) {
            if (traits.isEmpty()) break

            val selected = selectionManager.randomSelection(traits)
            traits.remove(selected)
            selectedTraits.add(selected)
        }

        // generate fields
        val additionalFields = (1..selectionManager.selectNumberOfFields()).map { generateField(classContext) }.toSet()
        val requiredFields = traits.map { it.fields() }.unionAll()
        val fields = requiredFields union additionalFields

        classContext.symbolTable.addAll(fields)

        // generate function methods
        val additionalFunctionMethods =
            (1..selectionManager.selectNumberOfFunctionMethods()).map { generateFunctionMethod(classContext) }.toSet()
        val requiredFunctionMethods = traits.map { it.functionMethods() }
            .unionAll()
            .map { signature -> generateFunctionMethod(classContext, signature) }
            .toSet()
        val functionMethods = requiredFunctionMethods union additionalFunctionMethods

        // generate methods
        val additionalMethods =
            (1..selectionManager.selectNumberOfMethods()).map { generateMethod(classContext) }.toSet()
        val requiredMethods = traits.map { it.methods() }
            .unionAll()
            .map { signature -> generateMethod(signature) }
            .toSet()
        val methods = requiredMethods union additionalMethods
        methods.forEach { method -> method.setBody(generateMethodBody(classContext, method)) }

        val clazz = ClassAST(classNameGenerator.newValue(), selectedTraits, functionMethods, methods, fields)

        context.globalSymbolTable.addClass(clazz)

        context.globalSymbolTable.addAllClasses(
            classContext.globalSymbolTable.classes() subtract context.globalSymbolTable.classes().toSet(),
        )
        context.globalSymbolTable.addAllTraits(
            classContext.globalSymbolTable.traits() subtract context.globalSymbolTable.traits().toSet(),
        )

        return clazz
    }

    override fun generateField(context: GenerationContext) =
        IdentifierAST(fieldNameGenerator.newValue(), selectionManager.selectType(context))

    override fun generateFunctionMethod(
        context: GenerationContext,
        targetType: Type?,
    ): FunctionMethodAST {
        val functionMethodSignature = generateFunctionMethodSignature(context, targetType)
        return generateFunctionMethod(context, functionMethodSignature)
    }

    private fun generateFunctionMethod(
        context: GenerationContext,
        signature: FunctionMethodSignatureAST,
    ): FunctionMethodAST {
        val functionContext = GenerationContext(context.globalSymbolTable, onDemandIdentifiers = false)
        signature.params.forEach { param -> functionContext.symbolTable.add(param) }

        val body = generateExpression(functionContext, signature.returnType).makeSafe()
        val functionMethodAST = FunctionMethodAST(signature, body)

        context.globalSymbolTable.addFunctionMethod(functionMethodAST)

        return functionMethodAST
    }

    override fun generateFunctionMethodSignature(
        context: GenerationContext,
        targetType: Type?,
    ): FunctionMethodSignatureAST {
        val name = functionMethodNameGenerator.newValue()
        val numberOfParameters = selectionManager.selectNumberOfParameters()

        val parameterNameGenerator = ParameterNameGenerator()
        val returnType = targetType ?: generateType(context, literalOnly = true)
        val parameters = (1..numberOfParameters).map {
            IdentifierAST(parameterNameGenerator.newValue(), returnType)
        }

        return FunctionMethodSignatureAST(name, returnType, parameters)
    }

    override fun generateMethod(context: GenerationContext): MethodAST =
        MethodAST(generateMethodSignature(context))

    private fun generateMethod(signature: MethodSignatureAST): MethodAST = MethodAST(signature)

    override fun generateMethodSignature(context: GenerationContext): MethodSignatureAST {
        val name = methodNameGenerator.newValue()
        val returnType = selectionManager.selectMethodReturnType(context)

        val returnsNameGenerator = ReturnsNameGenerator()
        val returns = returnType.map { t -> IdentifierAST(returnsNameGenerator.newValue(), t) }

        val numberOfParameters = selectionManager.selectNumberOfParameters()
        val parameterNameGenerator = ParameterNameGenerator()
        val parameters = (1..numberOfParameters).map {
            IdentifierAST(
                parameterNameGenerator.newValue(),
                generateType(context, literalOnly = true),
                mutable = false,
            )
        }

        return MethodSignatureAST(name, parameters, returns)
    }

    private fun generateMethodBody(context: GenerationContext, method: MethodAST): SequenceAST {
        val body = generateSequence(context, maxStatements = 10)

        val returnAssigns = method.returns().map { r ->
            val expr = generateExpression(context.disableOnDemand(), r.type()).makeSafe()
            AssignmentAST(r, expr)
        }

        return body.addStatements(context.clearDependentStatements() + returnAssigns)
    }

    override fun generateSequence(context: GenerationContext, maxStatements: Int): SequenceAST {
        val n = selectionManager.selectSequenceLength(maxStatements)

        val statements = (1..n).map { generateStatement(context) }

//        println("On demand statements")
//        println("${context.dependentStatements}")

        val allStatements = context.clearDependentStatements() + statements

        return SequenceAST(allStatements)
    }

    /* ==================================== STATEMENTS ==================================== */

    override fun generateStatement(context: GenerationContext): StatementAST = try {
        generateStatementFromType(selectionManager.selectStatementType(context), context)
    } catch (e: MethodOnDemandException) {
        generateStatementFromType(
            selectionManager.selectStatementType(context, methodCalls = false),
            context,
        )
    }

    private fun generateStatementFromType(
        type: StatementType,
        context: GenerationContext,
    ): StatementAST = when (type) {
        ASSIGN -> generateAssignmentStatement(context)
        CLASS_INSTANTIATION -> generateClassInstantiation(context)
        DECLARATION -> generateDeclarationStatement(context)
        IF -> generateIfStatement(context)
        METHOD_CALL -> generateMethodCall(context)
        PRINT -> generatePrintStatement(context)
        WHILE -> generateWhileStatement(context)
    }

    override fun generateIfStatement(context: GenerationContext): IfStatementAST {
        val condition = generateExpression(context, BoolType).makeSafe()

        val ifBranch = generateSequence(context.increaseStatementDepth())
        val elseBranch = generateSequence(context.increaseStatementDepth())

        return IfStatementAST(condition, ifBranch, elseBranch)
    }

    override fun generateWhileStatement(context: GenerationContext): WhileLoopAST {
        val counterIdentifierName = context.loopCounterGenerator.newValue()
        val counterIdentifier = IdentifierAST(counterIdentifierName, IntType)
        val counterInitialisation = DeclarationAST(counterIdentifier, IntegerLiteralAST(0))

        var condition: ExpressionAST
        do {
            condition = generateExpression(context, BoolType).makeSafe()
        } while (condition == BooleanLiteralAST(false)) // we don't want while(false) explicitly

        val counterTerminationCheck = IfStatementAST(
            BinaryExpressionAST(
                counterIdentifier,
                GreaterThanEqualOperator,
                IntegerLiteralAST(
                    DAFNY_MAX_LOOP_COUNTER,
                ),
            ),
            SequenceAST(listOf(BreakAST)),
            null,
        )

        val whileBody = generateSequence(context.increaseStatementDepth())

        val counterUpdate = AssignmentAST(
            counterIdentifier,
            BinaryExpressionAST(counterIdentifier, AdditionOperator, IntegerLiteralAST(1)),
        )

        return WhileLoopAST(
            counterInitialisation,
            counterTerminationCheck,
            condition,
            whileBody,
            counterUpdate,
        )
    }

    override fun generatePrintStatement(context: GenerationContext): PrintAST {
        val targetType = generateType(context, literalOnly = true)
        val safeExpr = generateExpression(context, targetType).makeSafe()
        return PrintAST(safeExpr)
    }

    override fun generateDeclarationStatement(context: GenerationContext): DeclarationAST {
        val targetType = generateType(context)
        return generateDeclarationStatementForType(context, targetType, false)
    }

    private fun generateDeclarationStatementForType(
        context: GenerationContext,
        targetType: Type,
        isLiteral: Boolean,
    ): DeclarationAST {
        val expr = if (isLiteral) {
            generateLiteralForType(context, targetType)
        } else {
            generateExpression(
                context,
                targetType,
            )
        }

        val identifierName = context.identifierNameGenerator.newValue()
        val identifier = if (targetType is ArrayType) {
            val length = if (expr is ArrayIdentifierAST) expr.length else (expr as ArrayInitAST).length
            ArrayIdentifierAST(identifierName, targetType, length)
        } else {
            IdentifierAST(identifierName, targetType)
        }

        context.symbolTable.add(identifier)

        val safeIdentifier = identifier.makeSafe()
        val safeExpr = expr.makeSafe()
        return DeclarationAST(safeIdentifier, safeExpr)
    }

    override fun generateAssignmentStatement(context: GenerationContext): AssignmentAST {
        val targetType = generateType(context, literalOnly = true)
        val identifier = if (selectionManager.randomWeightedSelection(listOf(true to 0.8, false to 0.2))) {
            generateIdentifier(context, targetType, mutableConstraint = true)
        } else {
            generateArrayIndex(context, targetType)
        }

        context.symbolTable.add(identifier)

        val expr = generateExpression(context, targetType)

        val safeIdentifier = identifier.makeSafe()
        val safeExpr = expr.makeSafe()
        return AssignmentAST(safeIdentifier, safeExpr)
    }

    override fun generateClassInstantiation(context: GenerationContext): DeclarationAST {
        // on demand create class if one doesn't exist
        if (!context.globalSymbolTable.hasClasses() || selectionManager.generateNewClass()) {
            generateClass(context)
        }

        val selectedClass = selectionManager.randomSelection(context.globalSymbolTable.classes())
        val requiredFields = selectedClass.fields

        val params = requiredFields.map { field -> generateExpression(context, field.type()).makeSafe() }
        val ident = IdentifierAST(context.identifierNameGenerator.newValue(), ClassType(selectedClass))

        context.symbolTable.add(ident)

        return DeclarationAST(ident, ClassInstantiationAST(selectedClass, params))
    }

    @Throws(MethodOnDemandException::class)
    override fun generateMethodCall(context: GenerationContext): StatementAST {
        // get callable methods
        val methods = context.globalSymbolTable.methods().filter { method ->
            context.methodContext == null || methodCallTable.canCall(context.methodContext, method)
        }

        // no support for on demand method generation within methods
        if (methods.isEmpty() && context.methodContext != null) {
            throw MethodOnDemandException("Callable method unavailable for context ${context.methodContext.name()}")
        }

        // generate new method if a callable method doesn't exist
        val method: MethodAST
        if (methods.isEmpty() || selectionManager.generateNewMethod(context)) {
            method = generateMethod(context)
            context.globalSymbolTable.addMethod(method)
        } else {
            method = selectionManager.randomSelection(methods)
        }

        val params = method.params().map { param -> generateExpression(context, param.type()).makeSafe() }

        // add call for method context
        if (context.methodContext != null) {
            methodCallTable.addCall(method, context.methodContext)
        }

        val returns = method.returns()

        return if (returns.isEmpty()) {
            // void method type
            VoidMethodCallAST(method, params)
        } else {
            // non-void method type
            val idents =
                returns.map { r -> IdentifierAST(context.identifierNameGenerator.newValue(), r.type()) }
            idents.forEach { ident -> context.symbolTable.add(ident) }
            MultiDeclarationAST(idents, listOf(NonVoidMethodCallAST(method, params)))
        }
    }

    /* ==================================== EXPRESSIONS ==================================== */

    override fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST {
        return try {
            generateExpressionFromType(
                selectionManager.selectExpressionType(targetType, context),
                context,
                targetType,
            )
        } catch (e: IdentifierOnDemandException) {
            generateExpressionFromType(
                selectionManager.selectExpressionType(targetType, context, identifier = false),
                context,
                targetType,
            )
        }
    }

    private fun generateExpressionFromType(
        exprType: ExpressionType,
        context: GenerationContext,
        targetType: Type,
    ): ExpressionAST = when (exprType) {
        UNARY -> generateUnaryExpression(context, targetType)
        BINARY -> generateBinaryExpression(context, targetType)
        FUNCTION_METHOD_CALL -> generateFunctionMethodCall(context, targetType)
        IDENTIFIER -> generateIdentifier(context, targetType)
        LITERAL -> generateLiteralForType(context, targetType)
    }

    override fun generateFunctionMethodCall(
        context: GenerationContext,
        targetType: Type,
    ): FunctionMethodCallAST {
        if (!context.globalSymbolTable.hasFunctionMethodType(targetType)) {
            generateFunctionMethod(context, targetType)
        }

        val selection = context.globalSymbolTable.withFunctionMethodType(targetType)
        val functionMethod = selectionManager.randomSelection(selection)

        val arguments = functionMethod.params().map { param ->
            generateExpression(context.increaseExpressionDepth(), param.type()).makeSafe()
        }

        return FunctionMethodCallAST(functionMethod, arguments)
    }

    @Throws(IdentifierOnDemandException::class)
    override fun generateIdentifier(
        context: GenerationContext,
        targetType: Type,
        mutableConstraint: Boolean,
    ): IdentifierAST {
        val withType = context.symbolTable.withType(targetType).filter {
            !mutableConstraint || it.mutable
        }

        if (withType.isEmpty()) {
            if (!context.onDemandIdentifiers) throw IdentifierOnDemandException("Identifier of type $targetType not available")

            val decl = generateDeclarationStatementForType(context, targetType, true)
            context.addDependentStatement(decl)
        }

        return selectionManager.randomSelection(
            context.symbolTable.withType(targetType).filter { !mutableConstraint || it.mutable },
        )
    }

    override fun generateArrayIndex(context: GenerationContext, targetType: Type): ArrayIndexAST {
        val ident = generateIdentifier(context, ArrayType(targetType))

        val identifier = ident as ArrayIdentifierAST
        val index = IntegerLiteralAST(generateDecimalLiteralValue(false), false)

        return ArrayIndexAST(identifier, index)
    }

    override fun generateUnaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): UnaryExpressionAST {
        val expr = generateExpression(context, targetType)
        val operator = selectionManager.selectUnaryOperator(targetType)

        return UnaryExpressionAST(expr, operator)
    }

    override fun generateBinaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): BinaryExpressionAST {
        val nextDepthContext = context.increaseExpressionDepth()
        val (operator, inputType) = selectionManager.selectBinaryOperator(targetType)
        val expr1 = generateExpression(nextDepthContext, inputType)
        val expr2 = generateExpression(nextDepthContext, inputType)

        return BinaryExpressionAST(expr1, operator, expr2)
    }

    override fun generateLiteralForType(
        context: GenerationContext,
        targetType: LiteralType,
    ): LiteralAST = when (targetType) {
        BoolType -> generateBooleanLiteral(context)
        CharType -> generateCharLiteral(context)
        IntType -> generateIntegerLiteral(context)
        RealType -> generateRealLiteral(context)
        // should not be possible to reach here
        else -> throw UnsupportedOperationException("Trying to generate literal for non-literal type $targetType")
    }

    private fun generateLiteralForType(
        context: GenerationContext,
        targetType: Type,
    ): ExpressionAST = when (targetType) {
        is LiteralType -> generateLiteralForType(context, targetType)
        is ArrayType ->
            if (context.expressionDepth > 1) {
                throw InvalidInputException("Generating array constructor in incorrect location")
            } else {
                generateArrayInitialisation(context, targetType)
            }

        else -> throw UnsupportedOperationException("Trying to generate literal for unsupported type")
    }

    override fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST {
        val isHex = selectionManager.randomWeightedSelection(listOf(true to 0.7, false to 0.3))
        val value = generateDecimalLiteralValue(negative = true)
        return IntegerLiteralAST(value, isHex)
    }

    override fun generateArrayInitialisation(
        context: GenerationContext,
        targetType: ArrayType,
    ): ArrayInitAST = ArrayInitAST(selectionManager.selectArrayLength(), targetType)

    override fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST =
        BooleanLiteralAST(selectionManager.selectBoolean())

    override fun generateRealLiteral(context: GenerationContext): RealLiteralAST {
        val beforePoint = generateDecimalLiteralValue(negative = true)
        val afterPoint = generateDecimalLiteralValue(negative = false)

        return RealLiteralAST("$beforePoint.$afterPoint")
    }

    override fun generateCharLiteral(context: GenerationContext): CharacterLiteralAST {
        val c = selectionManager.selectCharacter()
        return CharacterLiteralAST(c)
    }

    override fun generateType(context: GenerationContext, literalOnly: Boolean): Type =
        if (!context.onDemandIdentifiers) {
            selectionManager.randomSelection(listOf(IntType, BoolType))
        } else {
            selectionManager.selectType(context, literalOnly = literalOnly)
        }

    private fun generateDecimalLiteralValue(negative: Boolean = true): String {
        var value = selectionManager.selectDecimalLiteral()

        if (negative && selectionManager.randomWeightedSelection(
                listOf(
                    true to 0.2,
                    false to 0.8,
                ),
            )
        ) {
            value *= -1
        }

        return value.toString()
    }

    companion object {
        private val WRAPPER_FUNCTIONS = listOf(
            ABSOLUTE,
            SAFE_ADDITION_INT,
            SAFE_ADDITION_REAL,
            SAFE_DIVISION_INT,
            SAFE_DIVISION_REAL,
            SAFE_MODULO_INT,
            SAFE_MULTIPLY_INT,
            SAFE_MULTIPLY_REAL,
            SAFE_SUBTRACT_REAL,
            SAFE_SUBTRACT_CHAR,
            SAFE_SUBTRACT_INT,
        )
        private const val DAFNY_MAX_LOOP_COUNTER = 100
    }
}
