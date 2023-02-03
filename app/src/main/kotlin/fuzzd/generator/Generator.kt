package fuzzd.generator

import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIdentifierAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MainFunctionAST
import fuzzd.generator.ast.MethodAST
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
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.ArrayType
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.identifier_generator.NameGenerator.FunctionMethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.IdentifierNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.LoopCounterGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.MethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ParameterNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ReturnsNameGenerator
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.FUNCTION_METHOD_CALL
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.StatementType.ASSIGN
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.METHOD_CALL
import fuzzd.generator.selection.StatementType.PRINT
import fuzzd.generator.selection.StatementType.WHILE
import fuzzd.generator.symbol_table.GlobalSymbolTable
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
import kotlin.random.Random

class Generator(
    private val selectionManager: SelectionManager
) : ASTGenerator {
    private val random = Random.Default
    private val functionMethodNameGenerator = FunctionMethodNameGenerator()
    private val identifierNameGenerator = IdentifierNameGenerator()
    private val loopCounterGenerator = LoopCounterGenerator()
    private val methodNameGenerator = MethodNameGenerator()

    /* ==================================== TOP LEVEL ==================================== */

    override fun generate(): TopLevelAST {
        val context = GenerationContext(GlobalSymbolTable())

        // init with safety function methods
        context.globalSymbolTable.addAllFunctionMethods(WRAPPER_FUNCTIONS)

        val mainFunction = generateMainFunction(context)
        val ast = mutableListOf<ASTElement>()

        context.globalSymbolTable.methods().forEach { method ->
            val functionContext = GenerationContext(context.globalSymbolTable, methodContext = method)
            method.params.forEach { param -> functionContext.symbolTable.add(param) }
            method.returns.forEach { r -> functionContext.symbolTable.add(r) }

            val body = generateSequence(functionContext)
            method.setBody(body)
        }

        ast.addAll(context.globalSymbolTable.functionMethods())
        ast.addAll(context.globalSymbolTable.methods())
        ast.add(mainFunction)

        return TopLevelAST(ast)
    }

    override fun generateMainFunction(context: GenerationContext): MainFunctionAST {
        val body = generateSequence(context)
        val printContext = context.disableOnDemand()
        val prints = (1..20).map { generatePrintStatement(printContext) }
        return MainFunctionAST(body.addStatements(prints))
    }

    override fun generateFunctionMethod(context: GenerationContext, targetType: Type?): FunctionMethodAST {
        val name = functionMethodNameGenerator.newValue()
        val numberOfParameters = selectionManager.selectNumberOfParameters()

        val parameterNameGenerator = ParameterNameGenerator()
        val returnType = targetType ?: generateType(context, literalOnly = true)
        val parameters = (1..numberOfParameters).map {
            IdentifierAST(parameterNameGenerator.newValue(), returnType)
        }

        val functionContext = GenerationContext(context.globalSymbolTable, onDemandIdentifiers = false)
        parameters.forEach { param -> functionContext.symbolTable.add(param) }

        val body = generateExpression(functionContext, returnType).makeSafe()
        val functionMethodAST = FunctionMethodAST(name, returnType, parameters, body)

        context.globalSymbolTable.addFunctionMethod(functionMethodAST)

        return functionMethodAST
    }

    override fun generateMethod(context: GenerationContext): MethodAST {
        val name = methodNameGenerator.newValue()
        val returnType = selectionManager.selectMethodReturnType()

        val returnsNameGenerator = ReturnsNameGenerator()
        val returns = returnType.map { t -> IdentifierAST(returnsNameGenerator.newValue(), t) }

        val numberOfParameters = selectionManager.selectNumberOfParameters()
        val parameterNameGenerator = ParameterNameGenerator()
        val parameters = (1..numberOfParameters).map {
            IdentifierAST(
                parameterNameGenerator.newValue(),
                generateType(context, literalOnly = true),
                mutable = false
            )
        }

        val method = MethodAST(name, parameters, returns)
        context.globalSymbolTable.addMethod(method)
        return method
    }

    override fun generateSequence(context: GenerationContext): SequenceAST {
        val n = random.nextInt(1, 20)

        val statements = (1..n).map { generateStatement(context) }.toList()

        val allStatements = context.getDependentStatements() + statements
        context.clearDependentStatements()

        return SequenceAST(allStatements)
    }

    /* ==================================== STATEMENTS ==================================== */

    override fun generateStatement(context: GenerationContext): StatementAST =
        when (selectionManager.selectStatementType(context)) {
            ASSIGN -> generateAssignmentStatement(context)
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
        val counterIdentifierName = loopCounterGenerator.newValue()
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
                    DAFNY_MAX_LOOP_COUNTER
                )
            ),
            SequenceAST(listOf(BreakAST)),
            null
        )

        val whileBody = generateSequence(context.increaseStatementDepth())

        val counterUpdate = AssignmentAST(
            counterIdentifier,
            BinaryExpressionAST(counterIdentifier, AdditionOperator, IntegerLiteralAST(1))
        )

        return WhileLoopAST(counterInitialisation, counterTerminationCheck, condition, whileBody, counterUpdate)
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
        isLiteral: Boolean
    ): DeclarationAST {
        val expr =
            if (isLiteral) generateLiteralForType(context, targetType) else generateExpression(context, targetType)

        val identifierName = identifierNameGenerator.newValue()
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

    override fun generateMethodCall(context: GenerationContext): StatementAST {
        val method = if (context.globalSymbolTable.methods().isEmpty() || selectionManager.generateNewMethod(context)) {
            generateMethod(context)
        } else {
            selectionManager.randomSelection(context.globalSymbolTable.methods())
        }

        val params = method.params.map { param -> generateExpression(context, param.type()).makeSafe() }

        return if (method.returns.isEmpty()) {
            // void method type
            VoidMethodCallAST(method, params)
        } else {
            // non-void method type
            val idents = method.returns.map { r -> IdentifierAST(identifierNameGenerator.newValue(), r.type()) }
            idents.forEach { ident -> context.symbolTable.add(ident) }
            MultiDeclarationAST(idents, listOf(NonVoidMethodCallAST(method, params)))
        }
    }

    /* ==================================== EXPRESSIONS ==================================== */

    override fun generateExpression(context: GenerationContext, targetType: Type): ExpressionAST {
        val expr = when (selectionManager.selectExpressionType(targetType, context)) {
            UNARY -> generateUnaryExpression(context, targetType)
            BINARY -> generateBinaryExpression(context, targetType)
            FUNCTION_METHOD_CALL -> generateFunctionMethodCall(context, targetType)
            IDENTIFIER -> generateIdentifier(context, targetType)
            LITERAL -> generateLiteralForType(context, targetType)
        }
        return expr
    }

    override fun generateFunctionMethodCall(context: GenerationContext, targetType: Type): FunctionMethodCallAST {
        if (!context.globalSymbolTable.hasFunctionMethodType(targetType)) {
            generateFunctionMethod(context, targetType)
        }

        val selection = context.globalSymbolTable.withFunctionMethodType(targetType)
        val functionMethod = selectionManager.randomSelection(selection)

        val arguments = functionMethod.params.map { param ->
            generateExpression(context.increaseExpressionDepth(), param.type()).makeSafe()
        }

        return FunctionMethodCallAST(functionMethod, arguments)
    }

    override fun generateIdentifier(
        context: GenerationContext,
        targetType: Type,
        mutableConstraint: Boolean
    ): IdentifierAST {
        var withType = context.symbolTable.withType(targetType)

        if (mutableConstraint) {
            withType = withType.filter { it.mutable }
        }

        if (withType.isEmpty()) {
            val decl = generateDeclarationStatementForType(context, targetType, true)
            context.addDependentStatement(decl)
        }

        return selectionManager.randomSelection(context.symbolTable.withType(targetType).filter { it.mutable })
    }

    override fun generateArrayIndex(context: GenerationContext, targetType: Type): ArrayIndexAST {
        val identifier = generateIdentifier(context, ArrayType(targetType)) as ArrayIdentifierAST
        val index = IntegerLiteralAST(generateDecimalLiteralValue(false), false)

        return ArrayIndexAST(identifier, index)
    }

    override fun generateUnaryExpression(context: GenerationContext, targetType: Type): UnaryExpressionAST {
        val expr = generateExpression(context, targetType)
        val operator = selectionManager.selectUnaryOperator(targetType)

        return UnaryExpressionAST(expr, operator)
    }

    override fun generateBinaryExpression(context: GenerationContext, targetType: Type): BinaryExpressionAST {
        val nextDepthContext = context.increaseExpressionDepth()
        val (operator, inputType) = selectionManager.selectBinaryOperator(targetType)
        val expr1 = generateExpression(nextDepthContext, inputType)
        val expr2 = generateExpression(nextDepthContext, inputType)

        return BinaryExpressionAST(expr1, operator, expr2)
    }

    override fun generateLiteralForType(context: GenerationContext, targetType: LiteralType): LiteralAST =
        when (targetType) {
            BoolType -> generateBooleanLiteral(context)
            CharType -> generateCharLiteral(context)
            IntType -> generateIntegerLiteral(context)
            RealType -> generateRealLiteral(context)
            // should not be possible to reach here
            else -> throw UnsupportedOperationException("Trying to generate literal for non-literal type $targetType")
        }

    private fun generateLiteralForType(context: GenerationContext, targetType: Type): ExpressionAST =
        when (targetType) {
            is LiteralType -> generateLiteralForType(context, targetType)
            is ArrayType -> generateArrayInitialisation(context, targetType)
            else -> throw UnsupportedOperationException("Trying to generate literal for unsupported type")
        }

    override fun generateIntegerLiteral(context: GenerationContext): IntegerLiteralAST {
        val isHex = selectionManager.randomWeightedSelection(listOf(true to 0.7, false to 0.3))
        val value = generateDecimalLiteralValue(negative = true)
        return IntegerLiteralAST(value, isHex)
    }

    override fun generateArrayInitialisation(context: GenerationContext, targetType: ArrayType): ArrayInitAST =
        ArrayInitAST(selectionManager.selectArrayLength(), targetType)

    override fun generateBooleanLiteral(context: GenerationContext): BooleanLiteralAST =
        BooleanLiteralAST(random.nextBoolean())

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
            selectionManager.randomSelection(context.symbolTable.types())
        } else {
            selectionManager.selectType(literalOnly = literalOnly)
        }

    private fun generateDecimalLiteralValue(negative: Boolean = true): String {
        var value = selectionManager.selectDecimalLiteral()

        if (negative && selectionManager.randomWeightedSelection(listOf(true to 0.2, false to 0.8))) {
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
            SAFE_SUBTRACT_INT
        )
        private const val DAFNY_MAX_LOOP_COUNTER = 100
    }
}
