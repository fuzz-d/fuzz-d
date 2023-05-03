package fuzzd.generator

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ClassInstanceMethodSignatureAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.DatatypeConstructorAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstanceAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
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
import fuzzd.generator.ast.ExpressionAST.TopLevelDatatypeInstanceAST
import fuzzd.generator.ast.ExpressionAST.TraitInstanceAST
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
import fuzzd.generator.ast.StatementAST.CounterLimitedWhileLoopAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.TypedDeclarationAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ClassType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.DatatypeType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.Type.SetType
import fuzzd.generator.ast.Type.StringType
import fuzzd.generator.ast.Type.TopLevelDatatypeType
import fuzzd.generator.ast.Type.TraitType
import fuzzd.generator.ast.error.IdentifierOnDemandException
import fuzzd.generator.ast.error.MethodOnDemandException
import fuzzd.generator.ast.identifier_generator.NameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ClassNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ControlFlowGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.DatatypeConstructorFieldGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.DatatypeConstructorGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.DatatypeNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.FieldNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.FunctionMethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.MethodNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ParameterNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.ReturnsNameGenerator
import fuzzd.generator.ast.identifier_generator.NameGenerator.TraitNameGenerator
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.MembershipOperator
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.AssignType
import fuzzd.generator.selection.AssignType.ARRAY_INDEX
import fuzzd.generator.selection.ExpressionType
import fuzzd.generator.selection.ExpressionType.BINARY
import fuzzd.generator.selection.ExpressionType.CONSTRUCTOR
import fuzzd.generator.selection.ExpressionType.FUNCTION_METHOD_CALL
import fuzzd.generator.selection.ExpressionType.IDENTIFIER
import fuzzd.generator.selection.ExpressionType.INDEX
import fuzzd.generator.selection.ExpressionType.INDEX_ASSIGN
import fuzzd.generator.selection.ExpressionType.LITERAL
import fuzzd.generator.selection.ExpressionType.MATCH
import fuzzd.generator.selection.ExpressionType.MODULUS
import fuzzd.generator.selection.ExpressionType.MULTISET_CONVERSION
import fuzzd.generator.selection.ExpressionType.TERNARY
import fuzzd.generator.selection.ExpressionType.UNARY
import fuzzd.generator.selection.IndexType.ARRAY
import fuzzd.generator.selection.IndexType.DATATYPE
import fuzzd.generator.selection.IndexType.MAP
import fuzzd.generator.selection.IndexType.MULTISET
import fuzzd.generator.selection.IndexType.SEQUENCE
import fuzzd.generator.selection.IndexType.STRING
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.StatementType
import fuzzd.generator.selection.StatementType.ASSIGN
import fuzzd.generator.selection.StatementType.CLASS_INSTANTIATION
import fuzzd.generator.selection.StatementType.DECLARATION
import fuzzd.generator.selection.StatementType.IF
import fuzzd.generator.selection.StatementType.MAP_ASSIGN
import fuzzd.generator.selection.StatementType.METHOD_CALL
import fuzzd.generator.selection.StatementType.PRINT
import fuzzd.generator.selection.StatementType.WHILE
import fuzzd.generator.symbol_table.DependencyTable
import fuzzd.generator.symbol_table.FunctionSymbolTable
import fuzzd.generator.symbol_table.SymbolTable
import fuzzd.utils.foldPair
import fuzzd.utils.reduceLists
import fuzzd.utils.unionAll
import java.lang.Integer.max

class Generator(
    private val selectionManager: SelectionManager,
    private val instrument: Boolean = false,
) : ASTGenerator {
    private val classNameGenerator = ClassNameGenerator()
    private val fieldNameGenerator = FieldNameGenerator()
    private val functionMethodNameGenerator = FunctionMethodNameGenerator()
    private val methodNameGenerator = MethodNameGenerator()
    private val traitNameGenerator = TraitNameGenerator()
    private val datatypeNameGenerator = DatatypeNameGenerator()
    private val datatypeConstructorGenerator = DatatypeConstructorGenerator()

    private val methodCallTable = DependencyTable<MethodSignatureAST>()
    private val controlFlowGenerator = ControlFlowGenerator()

    /* ==================================== TOP LEVEL ==================================== */

    override fun generate(): DafnyAST {
        val context = GenerationContext(FunctionSymbolTable())

        (1..selectionManager.selectNumberOfFields()).map { generateDatatype(context) }

        val globalFields = (1..selectionManager.selectNumberOfGlobalFields()).map { generateField(context) }.toSet()
        val globalStateClass = ClassAST.builder().withName(GLOBAL_STATE).withFields(globalFields).build()
        context.setGlobalState(globalStateClass)

        val mainFunction = generateMainFunction(context)
        val ast = mutableListOf<TopLevelAST>()

        val methodBodyQueue = context.functionSymbolTable.methods().toMutableList()
        val visitedMethods = mutableSetOf<MethodAST>()

        while (methodBodyQueue.isNotEmpty()) {
            val method = methodBodyQueue.removeFirst()

            if (method in visitedMethods) continue
            visitedMethods.add(method)

            val functionContext = GenerationContext(
                context.functionSymbolTable,
                methodContext = method.signature,
            ).setGlobalState(context.globalState())
            val body = generateMethodBody(functionContext, method)
            method.setBody(body)

            methodBodyQueue.addAll(context.functionSymbolTable.methods() subtract methodBodyQueue.toSet() subtract visitedMethods)
        }

        ast.addAll(context.functionSymbolTable.datatypes())
        ast.add(context.globalState())
        ast.addAll(context.functionSymbolTable.functionMethods())
        ast.addAll(context.functionSymbolTable.methods())
        ast.addAll(context.functionSymbolTable.traits())
        ast.addAll(context.functionSymbolTable.classes())
        ast.add(mainFunction)

        return DafnyAST(ast)
    }

    override fun generateMainFunction(context: GenerationContext): MainFunctionAST {
        val (globalStateParams, globalStateDeps) = context.globalState().fields.map {
            if (it.type() is LiteralType) {
                generateLiteralForType(context, it.type() as LiteralType)
            } else {
                generateExpression(context.increaseExpressionDepth().disableFunctionCalls(), it.type())
            }
        }.foldPair()

        val globalStateIdentifier = ClassInstanceAST(context.globalState(), PARAM_GLOBAL_STATE)
        val globalStateDecl =
            DeclarationAST(globalStateIdentifier, ClassInstantiationAST(context.globalState(), globalStateParams))

        context.symbolTable.add(globalStateIdentifier)

        val body = generateSequence(context)
        return MainFunctionAST(SequenceAST(globalStateDeps + globalStateDecl + body.statements /*+ prints*/))
    }

    override fun generateTrait(context: GenerationContext): TraitAST {
        val fields = (1..selectionManager.selectNumberOfFields()).map { generateField(context) }.toSet()

        val functionMethods =
            (1..selectionManager.selectNumberOfFunctionMethods()).map { generateFunctionMethodSignature(context) }
                .toSet()

        val methods = (1..selectionManager.selectNumberOfMethods()).map { generateMethodSignature(context) }.toSet()

        val numberOfInherits = selectionManager.selectNumberOfTraitInherits()
        val selectedTraits = selectTraits(context, numberOfInherits)

        val trait = TraitAST(traitNameGenerator.newValue(), selectedTraits, functionMethods, methods, fields)

        context.functionSymbolTable.addTrait(trait)

        return trait
    }

    private fun selectTraits(context: GenerationContext, n: Int): Set<TraitAST> {
        val traits = context.functionSymbolTable.traits().toMutableList()
        val selectedTraits = mutableSetOf<TraitAST>()

        for (i in 1..n) {
            if (traits.isEmpty()) {
                traits.add(generateTrait(context))
            }

            val selected = selectionManager.randomSelection(traits)
            traits.remove(selected)
            selectedTraits.add(selected)
        }

        return selectedTraits
    }

    override fun generateClass(context: GenerationContext, mustExtend: List<TraitAST>): ClassAST {
        val classContext =
            GenerationContext(FunctionSymbolTable(context.functionSymbolTable.topLevel())).setGlobalState(context.globalState())

        // get traits
        val numberOfTraits = selectionManager.selectNumberOfTraits()
        val selectedTraits = selectTraits(context, max(0, numberOfTraits - mustExtend.size)) + mustExtend

        // generate fields
        val additionalFields = (1..selectionManager.selectNumberOfFields()).map { generateField(classContext) }
            .filter { it.type() !is TraitType || it.type() is TraitType && (it.type() as TraitType).trait !in selectedTraits }
            .toSet()
        val requiredFields = selectedTraits.map { it.fields() }.unionAll()

        classContext.symbolTable.addAll(additionalFields union requiredFields)

        // generate function methods
        val additionalFunctionMethods =
            (1..selectionManager.selectNumberOfFunctionMethods()).map { generateFunctionMethod(classContext) }.toSet()
        val requiredFunctionMethods = selectedTraits.map { it.functionMethods() }.unionAll()
            .map { signature -> generateFunctionMethod(classContext, signature) }.toSet()
        val functionMethods = requiredFunctionMethods union additionalFunctionMethods

        // generate methods
        val additionalMethods =
            (1..selectionManager.selectNumberOfMethods()).map { generateMethod(classContext) }.toSet()
        val requiredMethods =
            selectedTraits.map { it.methods() }.unionAll().map { signature -> generateMethod(signature) }.toSet()
        val methods = requiredMethods union additionalMethods
        methods.forEach { method ->
            method.setBody(
                generateMethodBody(
                    GenerationContext(
                        classContext.functionSymbolTable,
                        symbolTable = SymbolTable(classContext.symbolTable),
                        methodContext = method.signature,
                    ).setGlobalState(context.globalState()),
                    method,
                ),
            )
        }

        val clazz = ClassAST(
            classNameGenerator.newValue(),
            selectedTraits,
            functionMethods,
            methods,
            additionalFields,
            requiredFields,
        )

        // update symbol table with on-demand methods & classes generated into local global context
        context.functionSymbolTable.addFunctionMethods(classContext.functionSymbolTable.functionMethods() subtract functionMethods)
        context.functionSymbolTable.addMethods(classContext.functionSymbolTable.methods() subtract methods)
        context.functionSymbolTable.addClass(clazz)

        return clazz
    }

    override fun generateField(context: GenerationContext) = paramIdentifierFromType(
        generateType(context),
        fieldNameGenerator,
        mutable = true,
        initialised = true,
    )

    override fun generateFunctionMethod(context: GenerationContext, targetType: Type?): FunctionMethodAST {
        val functionMethodSignature = generateFunctionMethodSignature(context, targetType)
        return generateFunctionMethod(context, functionMethodSignature)
    }

    override fun generateDatatype(context: GenerationContext): DatatypeAST {
        val datatypeName = datatypeNameGenerator.newValue()
        val numberOfConstructors = selectionManager.selectNumberOfDatatypeConstructors()
        val fieldNameGenerator = DatatypeConstructorFieldGenerator()
        val constructors =
            (1..numberOfConstructors).map { generateDatatypeConstructor(context, fieldNameGenerator) }
        val datatype = DatatypeAST(datatypeName, constructors.toMutableList())

        if (numberOfConstructors > 0 && selectionManager.selectMakeDatatypeInductive()) {
            val constructor = DatatypeConstructorAST(
                datatypeConstructorGenerator.newValue(),
                listOf(TopLevelDatatypeInstanceAST(fieldNameGenerator.newValue(), TopLevelDatatypeType(datatype))),
            )
            datatype.constructors.add(constructor)
        }

        context.functionSymbolTable.addDatatype(datatype)
        return datatype
    }

    private fun generateFunctionMethod(
        context: GenerationContext,
        signature: FunctionMethodSignatureAST,
    ): FunctionMethodAST {
        val functionContext = GenerationContext(
            context.functionSymbolTable,
            onDemandIdentifiers = false,
        ).setGlobalState(context.globalState())
        signature.params.forEach { param -> functionContext.symbolTable.add(param) }

        val (body, _) = generateExpression(functionContext, signature.returnType)
        val functionMethodAST = FunctionMethodAST(signature, body)

        context.functionSymbolTable.addFunctionMethod(functionMethodAST)

        return functionMethodAST
    }

    override fun generateFunctionMethodSignature(
        context: GenerationContext,
        targetType: Type?,
    ): FunctionMethodSignatureAST {
        val name = functionMethodNameGenerator.newValue()
        val numberOfParameters = selectionManager.selectNumberOfParameters()

        val parameterNameGenerator = ParameterNameGenerator()
        val returnType = targetType ?: generateType(context.disableOnDemand())
        val parameters = (1..numberOfParameters).map {
            paramIdentifierFromType(
                generateType(context.disableOnDemand()),
                parameterNameGenerator,
                mutable = false,
                initialised = true,
            )
        }

        return FunctionMethodSignatureAST(
            name,
            returnType,
            parameters + listOf(ClassInstanceAST(context.globalState(), PARAM_GLOBAL_STATE)),
        )
    }

    private fun generateDatatype(context: GenerationContext, requiresConstructorType: Type): DatatypeAST {
        val datatypeName = datatypeNameGenerator.newValue()
        val fieldNameGenerator = DatatypeConstructorFieldGenerator()
        val compulsoryConstructor = DatatypeConstructorAST(
            datatypeConstructorGenerator.newValue(),
            listOf(
                paramIdentifierFromType(
                    requiresConstructorType,
                    fieldNameGenerator,
                    mutable = true,
                    initialised = true,
                ),
            ),
        )

        val numberOfConstructors = selectionManager.selectNumberOfDatatypeConstructors() - 1
        val otherConstructors = (1..numberOfConstructors).map {
            generateDatatypeConstructor(context, fieldNameGenerator)
        }
        val datatype = DatatypeAST(datatypeName, (otherConstructors + compulsoryConstructor).toMutableList())

        if (numberOfConstructors > 0 && selectionManager.selectMakeDatatypeInductive()) {
            val constructor = DatatypeConstructorAST(
                datatypeConstructorGenerator.newValue(),
                listOf(TopLevelDatatypeInstanceAST(fieldNameGenerator.newValue(), TopLevelDatatypeType(datatype))),
            )
            datatype.constructors.add(constructor)
        }

        context.functionSymbolTable.addDatatype(datatype)
        return datatype
    }

    private fun generateDatatypeConstructor(
        context: GenerationContext,
        fieldNameGenerator: NameGenerator,
    ): DatatypeConstructorAST {
        val numberOfFields = selectionManager.selectNumberOfDatatypeFields()
        val constructorName = datatypeConstructorGenerator.newValue()
        val fields = (1..numberOfFields).map {
            val type = generateType(context)
            paramIdentifierFromType(type, fieldNameGenerator, mutable = true, initialised = true)
        }
        return DatatypeConstructorAST(constructorName, fields.toMutableList())
    }

    override fun generateMethod(context: GenerationContext): MethodAST {
        val method = MethodAST(generateMethodSignature(context))
        context.functionSymbolTable.addMethod(method)
        return method
    }

    private fun generateMethod(signature: MethodSignatureAST): MethodAST = MethodAST(signature)

    private fun paramIdentifierFromType(
        type: Type,
        nameGenerator: NameGenerator,
        mutable: Boolean,
        initialised: Boolean,
    ): IdentifierAST = when (type) {
        is ClassType -> ClassInstanceAST(
            type.clazz,
            nameGenerator.newValue(),
            mutable = mutable,
            initialised = initialised,
        )

        is TraitType -> TraitInstanceAST(
            type.trait,
            nameGenerator.newValue(),
            mutable = mutable,
            initialised = initialised,
        )

        else -> IdentifierAST(nameGenerator.newValue(), type, mutable = mutable, initialised = initialised)
    }

    override fun generateMethodSignature(context: GenerationContext): MethodSignatureAST {
        val name = methodNameGenerator.newValue()
        val returnType = selectionManager.selectMethodReturnType(context)

        val returnsNameGenerator = ReturnsNameGenerator()
        val returns = returnType.map { t ->
            paramIdentifierFromType(t, returnsNameGenerator, mutable = true, initialised = false)
        }

        val numberOfParameters = selectionManager.selectNumberOfParameters()
        val parameterNameGenerator = ParameterNameGenerator()
        val parameters = (1..numberOfParameters).map {
            val type = generateType(context)
            paramIdentifierFromType(type, parameterNameGenerator, mutable = false, initialised = true)
        }

        return MethodSignatureAST(
            name,
            parameters + listOf(ClassInstanceAST(context.globalState(), PARAM_GLOBAL_STATE)),
            returns,
        )
    }

    private fun generateMethodBody(context: GenerationContext, method: MethodAST): SequenceAST {
        method.params().forEach { param -> context.symbolTable.add(param) }
        method.returns().forEach { r -> context.symbolTable.add(r) }

        val body = generateSequence(context, maxStatements = 10)

        val returnAssigns = method.returns().map { r ->
            val (expr, deps) = generateExpression(context, r.type())
            deps + AssignmentAST(r, expr)
        }.reduceLists()

        return body.addStatements(returnAssigns)
    }

    override fun generateSequence(context: GenerationContext, maxStatements: Int): SequenceAST {
        val n = selectionManager.selectSequenceLength(maxStatements)
        val statements = (1..n).map { generateStatement(context) }.reduceLists()
        return SequenceAST(statements)
    }

    /* ==================================== STATEMENTS ==================================== */

    override fun generateStatement(context: GenerationContext): List<StatementAST> = try {
        val type = selectionManager.selectStatementType(context)
        generateStatementFromType(type, context)
    } catch (e: MethodOnDemandException) {
        val type = selectionManager.selectStatementType(context, methodCalls = false)
        generateStatementFromType(type, context)
    }

    private fun generateStatementFromType(
        type: StatementType,
        context: GenerationContext,
    ): List<StatementAST> = when (type) {
        ASSIGN -> generateAssignmentStatement(context)
        CLASS_INSTANTIATION -> generateClassInstantiation(context)
        DECLARATION -> generateDeclarationStatement(context)
        IF -> generateIfStatement(context)
        MAP_ASSIGN -> generateMapAssign(context)
        METHOD_CALL -> generateMethodCall(context)
        PRINT -> generatePrintStatement(context)
        WHILE -> generateWhileStatement(context)
        StatementType.MATCH -> generateMatchStatement(context)
    }

    override fun generateMatchStatement(context: GenerationContext): List<StatementAST> {
        if (!context.functionSymbolTable.hasAvailableDatatypes(context.onDemandIdentifiers)) {
            generateDatatype(context)
        }
        val datatype = selectionManager.selectDatatypeType(context, 1)
        val (match, matchDeps) = generateExpression(context, datatype)

        val cases = datatype.datatype.datatypes().map { dtype ->
            val case = generateCaseMatchForDatatype(dtype)
            val caseContext = context.increaseStatementDepth()
            dtype.constructor.fields.forEach { caseContext.symbolTable.add(it) }
            val seq = generateSequence(caseContext, maxStatements = 5)
            Pair(case, seq)
        }

        return matchDeps + MatchStatementAST(match, cases)
    }

    override fun generateIfStatement(context: GenerationContext): List<StatementAST> {
        val (condition, conditionDeps) = generateExpression(context, BoolType)

        val ifPrint = PrintAST(StringLiteralAST(controlFlowGenerator.newValue()))
        val elsePrint = PrintAST(StringLiteralAST(controlFlowGenerator.newValue()))

        val ifBranch = generateSequence(context.increaseStatementDepth())
        val elseBranch = generateSequence(context.increaseStatementDepth())

        val ifStatement = IfStatementAST(
            condition,
            SequenceAST((if (instrument) listOf(ifPrint) else emptyList<StatementAST>()) + ifBranch.statements),
            SequenceAST((if (instrument) listOf(elsePrint) else emptyList<StatementAST>()) + elseBranch.statements),
        )

        return conditionDeps + ifStatement
    }

    override fun generateWhileStatement(context: GenerationContext): List<StatementAST> {
        val counterIdentifierName = context.loopCounterGenerator.newValue()
        val counterIdentifier = IdentifierAST(counterIdentifierName, IntType)
        val counterInitialisation = DeclarationAST(counterIdentifier, IntegerLiteralAST(0))

        val (condition, conditionDeps) = generateExpression(context, BoolType)

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

        val counterUpdate = AssignmentAST(
            counterIdentifier,
            BinaryExpressionAST(counterIdentifier, AdditionOperator, IntegerLiteralAST(1)),
        )

        val whileBody = generateSequence(context.increaseStatementDepth())
        val whileLoop = CounterLimitedWhileLoopAST(
            counterInitialisation,
            counterTerminationCheck,
            counterUpdate,
            condition,
            whileBody,
        )
        return conditionDeps + whileLoop
    }

    override fun generatePrintStatement(context: GenerationContext): List<StatementAST> {
        val targetType = generateType(context.disableOnDemand())
        val (expr, exprDeps) = generateExpression(context, targetType)
        return exprDeps + PrintAST(expr)
    }

    override fun generateDeclarationStatement(context: GenerationContext): List<StatementAST> =
        generateDeclarationStatementForType(context, generateType(context), false)

    private fun generateDeclarationStatementForType(
        context: GenerationContext,
        targetType: Type,
        baseExpression: Boolean,
    ): List<StatementAST> {
        val (expr, exprDeps) = if (baseExpression) {
            generateBaseExpressionForType(context, targetType)
        } else {
            generateExpression(context, targetType)
        }

        val identifierName = context.identifierNameGenerator.newValue()
        val identifier = when (targetType) {
            is ClassType -> ClassInstanceAST(targetType.clazz, identifierName, mutable = true, initialised = true)
            is TraitType -> TraitInstanceAST(targetType.trait, identifierName, mutable = true, initialised = true)
            is DatatypeType -> DatatypeInstanceAST(identifierName, targetType, mutable = true, initialised = true)
            is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(identifierName, targetType, mutable = true, initialised = true)
            else -> IdentifierAST(identifierName, targetType, initialised = true)
        }

        context.symbolTable.add(identifier)

        return exprDeps + if (targetType.requiresTypeAnnotation()) {
            TypedDeclarationAST(identifier, expr)
        } else {
            DeclarationAST(identifier, expr)
        }
    }

    override fun generateAssignmentStatement(context: GenerationContext): List<StatementAST> {
        val targetType = generateType(context)
        val (identifier, identDeps) = when (selectionManager.selectAssignType(context)) {
            AssignType.IDENTIFIER -> generateIdentifier(
                context,
                targetType,
                mutableConstraint = true,
                initialisedConstraint = false,
            )

            ARRAY_INDEX -> generateArrayIndex(context, targetType)
        }

        context.symbolTable.add(identifier.initialise())

        val (expr, exprDeps) = generateExpression(context, targetType)

        return identDeps + exprDeps + AssignmentAST(identifier, expr)
    }

    override fun generateClassInstantiation(context: GenerationContext): List<StatementAST> {
        // on demand create class if one doesn't exist
        if (!context.functionSymbolTable.hasClasses()) {
            generateClass(context)
        }

        val selectedClass = selectionManager.randomSelection(context.functionSymbolTable.classes().toList())
        val requiredFields = selectedClass.constructorFields

        val (params, paramDeps) = requiredFields.map { field ->
            generateExpression(context.increaseExpressionDepth(), field.type())
        }.foldPair()

        val ident = ClassInstanceAST(selectedClass, context.identifierNameGenerator.newValue())

        context.symbolTable.add(ident)

        return paramDeps + DeclarationAST(ident, ClassInstantiationAST(selectedClass, params))
    }

    @Throws(MethodOnDemandException::class)
    override fun generateMethodCall(context: GenerationContext): List<StatementAST> {
        // get callable methods
        val methods = (
                context.functionSymbolTable.methods().map { it.signature } +
                        context.symbolTable.classInstances().map { it.methods }.unionAll() +
                        context.symbolTable.traitInstances().map { it.methods }.unionAll()
                )
            .filter { method ->
                context.methodContext == null ||
                        method is ClassInstanceMethodSignatureAST &&
                        methodCallTable.canUseDependency(context.methodContext, method.signature) ||
                        method !is ClassInstanceMethodSignatureAST &&
                        methodCallTable.canUseDependency(context.methodContext, method)
            }

        // no support for on demand method generation within methods
        if (methods.isEmpty() && context.methodContext != null) {
            throw MethodOnDemandException("Callable method unavailable for context ${context.methodContext.name}")
        }

        // generate new method if a callable method doesn't exist
        val method = if (methods.isEmpty()) {
            generateMethod(context).signature
        } else {
            selectionManager.randomSelection(methods)
        }

        val (params, paramDeps) = method.params.subList(0, method.params.size - 1)
            .map { param -> generateExpression(context.increaseExpressionDepth(), param.type()) }.foldPair()

        // add call for method context
        if (context.methodContext != null) {
            methodCallTable.addDependency(method, context.methodContext)
        }

        val returns = method.returns
        val stateParam = ClassInstanceAST(context.globalState(), PARAM_GLOBAL_STATE)

        return paramDeps + if (returns.isEmpty()) {
            // void method type
            VoidMethodCallAST(method, params + stateParam)
        } else {
            // non-void method type
            val idents = returns.map { r ->
                paramIdentifierFromType(r.type(), context.identifierNameGenerator, mutable = true, initialised = true)
            }
            idents.forEach { ident -> context.symbolTable.add(ident) }
            MultiDeclarationAST(idents, listOf(NonVoidMethodCallAST(method, params + stateParam)))
        }
    }

    override fun generateMapAssign(context: GenerationContext): List<StatementAST> {
        val keyType = generateType(context)
        val valueType = generateType(context)
        val mapType = MapType(keyType, valueType)

        val (map, mapDeps) = generateIdentifier(
            context,
            mapType,
            mutableConstraint = true,
            initialisedConstraint = true,
        )
        val (key, keyDeps) = generateExpression(context.increaseExpressionDepth(), keyType)
        val (value, valueDeps) = generateExpression(context.increaseExpressionDepth(), valueType)

        return mapDeps + keyDeps + valueDeps + AssignmentAST(map, IndexAssignAST(map, key, value))
    }

    /* ==================================== EXPRESSIONS ==================================== */

    override fun generateExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> = try {
        val expressionType = selectionManager.selectExpressionType(targetType, context)
        generateExpressionFromType(
            expressionType,
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

    fun generateTopLevelDatatypeInstantiation(
        context: GenerationContext,
        targetType: TopLevelDatatypeType,
    ): Pair<DatatypeInstantiationAST, List<StatementAST>> {
        val datatype = targetType.datatype
        val availableDatatypes = context.functionSymbolTable.availableDatatypes(context.onDemandIdentifiers)
        val type = selectionManager.randomSelection(datatype.datatypes().filter { it in availableDatatypes })

        return generateDatatypeInstantiation(context, type)
    }

    override fun generateDatatypeInstantiation(
        context: GenerationContext,
        targetType: DatatypeType,
    ): Pair<DatatypeInstantiationAST, List<StatementAST>> {
        val datatype = targetType.datatype
        val constructor = targetType.constructor

        val (params, paramDeps) = constructor.fields.map {
            generateExpression(context.increaseExpressionDepth(), it.type())
        }.foldPair()

        return Pair(DatatypeInstantiationAST(datatype, constructor, params), paramDeps)
    }

    private fun generateCaseMatchForDatatype(datatype: DatatypeType): DatatypeInstantiationAST =
        DatatypeInstantiationAST(datatype.datatype, datatype.constructor, datatype.constructor.fields)

    override fun generateMatchExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MatchExpressionAST, List<StatementAST>> {
        if (!context.functionSymbolTable.hasAvailableDatatypes(onDemand = false)) {
            generateDatatype(context)
        }

        val datatype = selectionManager.selectDatatypeType(context.disableOnDemand(), 1)
        val (match, matchDeps) = generateExpression(context.increaseExpressionDepth(), datatype)
        val (cases, caseDeps) = datatype.datatype.datatypes().map { dtype ->
            val caseMatch = generateCaseMatchForDatatype(dtype)
            val exprContext = context.increaseExpressionDepthWithSymbolTable().disableOnDemand()
            dtype.constructor.fields.forEach { exprContext.symbolTable.add(it) }
            val (expr, exprDeps) = generateExpression(exprContext, targetType)
            Pair(Pair(caseMatch, expr), exprDeps)
        }.foldPair()

        return Pair(MatchExpressionAST(match, targetType, cases), matchDeps + caseDeps)
    }

    private fun generateExpressionFromType(
        exprType: ExpressionType,
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> = when (exprType) {
        CONSTRUCTOR -> generateConstructorForType(context, targetType)
        UNARY -> generateUnaryExpression(context, targetType)
        MODULUS -> generateModulus(context, targetType)
        MULTISET_CONVERSION -> generateMultisetConversion(context, targetType)
        BINARY -> generateBinaryExpression(context, targetType)
        TERNARY -> generateTernaryExpression(context, targetType)
        MATCH -> generateMatchExpression(context, targetType)
        FUNCTION_METHOD_CALL -> generateFunctionMethodCall(context, targetType)
        IDENTIFIER -> generateIdentifier(context, targetType, initialisedConstraint = true)
        LITERAL -> generateLiteralForType(context, targetType as LiteralType)
        INDEX_ASSIGN -> generateIndexAssign(context, targetType)
        INDEX -> generateIndex(context, targetType)
    }

    override fun generateFunctionMethodCall(
        context: GenerationContext,
        targetType: Type,
    ): Pair<FunctionMethodCallAST, List<StatementAST>> {
        val callable = callableFunctionMethods(context, targetType)
        val functionMethod = if (callable.isEmpty()) {
            generateFunctionMethod(context, targetType).signature
        } else {
            selectionManager.randomSelection(callable)
        }

        val (arguments, deps) = functionMethod.params.subList(0, functionMethod.params.size - 1).map { param ->
            generateExpression(context.increaseExpressionDepth(), param.type())
        }.foldPair()

        val fmCall = FunctionMethodCallAST(
            functionMethod,
            arguments + ClassInstanceAST(context.globalState(), PARAM_GLOBAL_STATE),
        )
        return Pair(fmCall, deps)
    }

    private fun callableFunctionMethods(
        context: GenerationContext,
        targetType: Type,
    ): List<FunctionMethodSignatureAST> = functionMethodsWithType(context, targetType).filter { fm ->
        !fm.params.map { p -> p.type() }.any { t -> t.hasHeapType() && !context.symbolTable.hasType(t) }
    }

    private fun functionMethodsWithType(
        context: GenerationContext,
        targetType: Type,
    ): List<FunctionMethodSignatureAST> =
        (context.functionSymbolTable.withFunctionMethodType(targetType).map { it.signature } +
                context.symbolTable.classInstances().map { it.functionMethods }.unionAll() +
                context.symbolTable.traitInstances().map { it.functionMethods }.unionAll())
            .filter { it.returnType == targetType }

    @Throws(IdentifierOnDemandException::class)
    override fun generateIdentifier(
        context: GenerationContext,
        targetType: Type,
        mutableConstraint: Boolean,
        initialisedConstraint: Boolean,
    ): Pair<IdentifierAST, List<StatementAST>> {
        val withType = context.symbolTable.withType(targetType).filter {
            (!mutableConstraint || it.mutable) && (!initialisedConstraint || it.initialised())
        }

        val deps = mutableListOf<StatementAST>()

        if (withType.isEmpty()) {
            if (!context.onDemandIdentifiers) throw IdentifierOnDemandException("Identifier of type $targetType not available")

            val decl = generateDeclarationStatementForType(context, targetType, true)
            deps += decl
        }

        val identifier = selectionManager.randomSelection(
            context.symbolTable.withType(targetType)
                .filter { (!mutableConstraint || it.mutable) && (!initialisedConstraint || it.initialised()) },
        )

        return Pair(identifier, deps)
    }

    override fun generateArrayIndex(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ArrayIndexAST, List<StatementAST>> {
        val (identifier, deps) = generateIdentifier(context, ArrayType(targetType), initialisedConstraint = true)
        val index = IntegerLiteralAST(generateDecimalLiteralValue(false), false)

        return Pair(ArrayIndexAST(identifier, index), deps)
    }

    override fun generateSetDisplay(
        context: GenerationContext,
        targetType: Type,
    ): Pair<SetDisplayAST, List<StatementAST>> {
        val (isMultiset, innerType) = if (targetType is MultisetType) {
            Pair(true, targetType.innerType)
        } else {
            Pair(false, (targetType as SetType).innerType)
        }

        val numberOfExpressions = selectionManager.selectNumberOfConstructorFields(context)
        val (exprs, exprDeps) = (1..numberOfExpressions).map {
            generateExpression(
                context.increaseExpressionDepth(),
                innerType,
            )
        }.foldPair()
        return Pair(SetDisplayAST(exprs, isMultiset), exprDeps)
    }

    override fun generateSequenceDisplay(
        context: GenerationContext,
        targetType: Type,
    ): Pair<SequenceDisplayAST, List<StatementAST>> {
        val seqType = targetType as SequenceType
        val numberOfExpressions = selectionManager.selectNumberOfConstructorFields(context)
        val (exprs, exprDeps) = (1..numberOfExpressions).map {
            generateExpression(
                context.increaseExpressionDepth(),
                seqType.innerType,
            )
        }.foldPair()

        return Pair(SequenceDisplayAST(exprs), exprDeps)
    }

    override fun generateMapConstructor(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MapConstructorAST, List<StatementAST>> {
        val mapType = targetType as MapType
        val numberOfExpressions = 1 // selectionManager.selectNumberOfConstructorFields(context) - https://github.com/dafny-lang/dafny/issues/3856
        val exprContext = context.increaseExpressionDepth()
        val (assigns, assignDeps) = (1..numberOfExpressions).map {
            val (key, keyDeps) = generateExpression(exprContext, mapType.keyType)
            val (value, valueDeps) = generateExpression(exprContext, mapType.valueType)
            Pair(Pair(key, value), keyDeps + valueDeps)
        }.foldPair()

        return Pair(MapConstructorAST(mapType.keyType, mapType.valueType, assigns), assignDeps)
    }

    override fun generateIndexAssign(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> = when (targetType) {
        is MapType -> generateMapIndexAssign(context, targetType)
        is MultisetType -> generateMultisetIndexAssign(context, targetType)
        is SequenceType -> generateSequenceIndexAssign(context, targetType)
        is DatatypeType -> generateDatatypeUpdate(context, targetType)
        else -> throw UnsupportedOperationException()
    }

    private fun generateMapIndexAssign(
        context: GenerationContext,
        targetType: MapType,
    ): Pair<IndexAssignAST, List<StatementAST>> {
        val (ident, identDeps) = generateExpression(context.increaseExpressionDepth(), targetType)
        val (index, indexDeps) = generateExpression(context.increaseExpressionDepth(), targetType.keyType)
        val (newValue, newValueDeps) = generateExpression(context.increaseExpressionDepth(), targetType.valueType)

        return Pair(IndexAssignAST(ident, index, newValue), identDeps + indexDeps + newValueDeps)
    }

    private fun generateMultisetIndexAssign(
        context: GenerationContext,
        targetType: MultisetType,
    ): Pair<IndexAssignAST, List<StatementAST>> {
        val (ident, identDeps) = generateExpression(context.increaseExpressionDepth(), targetType)
        val (index, indexDeps) = generateExpression(context.increaseExpressionDepth(), targetType.innerType)
        val (newValue, newValueDeps) = generateExpression(context.increaseExpressionDepth(), IntType)

        return Pair(IndexAssignAST(ident, index, newValue), identDeps + indexDeps + newValueDeps)
    }

    private fun generateSequenceIndexAssign(
        context: GenerationContext,
        targetType: SequenceType,
    ): Pair<IndexAssignAST, List<StatementAST>> {
        val (ident, identDeps) = generateExpression(context, targetType)
        val (index, indexDeps) = generateExpression(context.increaseExpressionDepth(), IntType)
        val (newValue, newValueDeps) = generateExpression(context.increaseExpressionDepth(), targetType.innerType)

        return Pair(IndexAssignAST(ident, index, newValue), identDeps + indexDeps + newValueDeps)
    }

    private fun generateDatatypeUpdate(
        context: GenerationContext,
        targetType: DatatypeType,
    ): Pair<DatatypeUpdateAST, List<StatementAST>> {
        val (datatypeInstance, instanceDeps) = generateExpression(context.increaseExpressionDepth(), targetType)
        val numberOfUpdates = selectionManager.selectInt(1, targetType.constructor.fields.size)
        val updateFields = (1..numberOfUpdates)
            .map { selectionManager.randomSelection(targetType.constructor.fields) }
            .toSet()

        val (updates, updateDeps) = updateFields.map { field ->
            val (expr, exprDeps) = generateExpression(context.increaseExpressionDepth(), field.type())
            Pair(Pair(field, expr), exprDeps)
        }.foldPair()

        return Pair(DatatypeUpdateAST(datatypeInstance, updates), instanceDeps + updateDeps)
    }

    override fun generateIndex(context: GenerationContext, targetType: Type): Pair<ExpressionAST, List<StatementAST>> =
        when (selectionManager.selectIndexType(context, targetType)) {
            ARRAY -> generateIdentifier(context, targetType)
            MAP -> {
                val keyType = generateType(context)
                generateMapIndex(context, keyType, targetType)
            }

            MULTISET -> {
                val keyType = generateType(context)
                generateMultisetIndex(context, keyType)
            }

            SEQUENCE -> generateSequenceIndex(context, targetType)

            STRING -> generateStringIndex(context)

            DATATYPE -> generateDatatypeDestructor(context, targetType)
        }

    private fun datatypesWithType(context: GenerationContext, type: Type): List<DatatypeType> =
        context.functionSymbolTable.availableDatatypes(context.onDemandIdentifiers)
            .filter { it.constructor.fields.any { f -> f.type() == type } }

    private fun generateDatatypeDestructor(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> {
        var availableDatatypes = datatypesWithType(context, targetType)
        if (availableDatatypes.isEmpty()) {
            generateDatatype(context, targetType)
            availableDatatypes = datatypesWithType(context, targetType)
        }

        val selectedDatatype = selectionManager.randomSelection(availableDatatypes)
        val (datatypeInstance, instanceDeps) = generateExpression(context.increaseExpressionDepth(), selectedDatatype)
        val field = selectionManager.randomSelection(
            selectedDatatype.constructor.fields.filter { it.type() == targetType },
        )

        return Pair(DatatypeDestructorAST(datatypeInstance, field), instanceDeps)
    }

    private fun generateMapIndex(
        context: GenerationContext,
        keyType: Type,
        valueType: Type,
    ): Pair<TernaryExpressionAST, List<StatementAST>> {
        val (ident, identDeps) = generateIdentifier(context, MapType(keyType, valueType), initialisedConstraint = true)
        val (indexKey, indexKeyDeps) = generateExpression(context.increaseExpressionDepth(), keyType)
        val (altExpr, altExprDeps) = generateExpression(context.increaseExpressionDepth(), valueType)

        val index = MapIndexAST(ident, indexKey)
        val ternaryExpression =
            TernaryExpressionAST(BinaryExpressionAST(indexKey, MembershipOperator, ident), index, altExpr)

        return Pair(ternaryExpression, identDeps + indexKeyDeps + altExprDeps)
    }

    private fun generateMultisetIndex(
        context: GenerationContext,
        keyType: Type,
    ): Pair<TernaryExpressionAST, List<StatementAST>> {
        val (ident, identDeps) = generateIdentifier(context, MultisetType(keyType), initialisedConstraint = true)
        val (indexKey, indexKeyDeps) = generateExpression(context.increaseExpressionDepth(), keyType)
        val (altExpr, altExprDeps) = generateExpression(context.increaseExpressionDepth(), IntType)

        val index = MultisetIndexAST(ident, indexKey)
        val ternaryExpression =
            TernaryExpressionAST(BinaryExpressionAST(indexKey, MembershipOperator, ident), index, altExpr)

        return Pair(ternaryExpression, identDeps + indexKeyDeps + altExprDeps)
    }

    private fun generateSequenceIndex(
        context: GenerationContext,
        targetType: Type,
    ): Pair<IndexAST, List<StatementAST>> {
        val (ident, identDeps) = generateIdentifier(context, SequenceType(targetType), initialisedConstraint = true)
        val (index, indexDeps) = generateExpression(context.increaseExpressionDepth(), IntType)
        return Pair(SequenceIndexAST(ident, index), identDeps + indexDeps)
    }

    private fun generateStringIndex(context: GenerationContext): Pair<IndexAST, List<StatementAST>> {
        val (ident, identDeps) = generateIdentifier(context, StringType, initialisedConstraint = true)
        val (index, indexDeps) = generateExpression(context.increaseExpressionDepth(), IntType)
        return Pair(SequenceIndexAST(ident, index), identDeps + indexDeps)
    }

    override fun generateUnaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<UnaryExpressionAST, List<StatementAST>> {
        val (expr, exprDeps) = generateExpression(context, targetType)
        val operator = selectionManager.selectUnaryOperator(targetType)

        return Pair(UnaryExpressionAST(expr, operator), exprDeps)
    }

    override fun generateModulus(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ModulusExpressionAST, List<StatementAST>> {
        val innerType = selectionManager.selectDataStructureType(context, 1)
        val (expr, exprDeps) = generateExpression(context, innerType)
        return Pair(ModulusExpressionAST(expr), exprDeps)
    }

    override fun generateMultisetConversion(
        context: GenerationContext,
        targetType: Type,
    ): Pair<MultisetConversionAST, List<StatementAST>> {
        val multisetType = targetType as MultisetType
        val (expr, exprDeps) = generateExpression(context, SequenceType(multisetType.innerType))

        return Pair(MultisetConversionAST(expr), exprDeps)
    }

    override fun generateBinaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<BinaryExpressionAST, List<StatementAST>> {
        val nextDepthContext = context.increaseExpressionDepth()
        val (operator, type) = selectionManager.selectBinaryOperator(context, targetType)

        val (expr1, expr1Deps) = generateExpression(nextDepthContext, type.first)
        val (expr2, expr2Deps) = generateExpression(nextDepthContext, type.second)

        return Pair(BinaryExpressionAST(expr1, operator, expr2), expr1Deps + expr2Deps)
    }

    override fun generateTernaryExpression(
        context: GenerationContext,
        targetType: Type,
    ): Pair<TernaryExpressionAST, List<StatementAST>> {
        val (condition, conditionDeps) = generateExpression(context.increaseExpressionDepth(), BoolType)
        val (ifExpr, ifExprDeps) = generateExpression(context.increaseExpressionDepth(), targetType)
        val (elseExpr, elseExprDeps) = generateExpression(context.increaseExpressionDepth(), targetType)

        return Pair(TernaryExpressionAST(condition, ifExpr, elseExpr), conditionDeps + ifExprDeps + elseExprDeps)
    }

    override fun generateBaseExpressionForType(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> = when (targetType) {
        is LiteralType -> generateLiteralForType(context, targetType)
        is ArrayType -> generateArrayInitialisation(context, targetType)
        is ClassType -> generateClassConstructor(context, targetType)
        is TraitType -> generateTraitConstructor(context, targetType)
        is DatatypeType -> generateDatatypeInstantiation(context, targetType)
        is TopLevelDatatypeType -> generateTopLevelDatatypeInstantiation(context, targetType)
        is MapType -> generateMapConstructor(context, targetType)
        is SetType, is MultisetType -> generateSetDisplay(context, targetType)
        is StringType -> generateStringLiteral(context)
        is SequenceType -> generateSequenceDisplay(context, targetType)
        // should not be possible to reach here
        else -> throw UnsupportedOperationException("Trying to generate base for non-base type $targetType")
    }

    private fun generateConstructorForType(
        context: GenerationContext,
        targetType: Type,
    ): Pair<ExpressionAST, List<StatementAST>> = when (targetType) {
        is ClassType -> generateClassConstructor(context, targetType)
        is TraitType -> generateTraitConstructor(context, targetType)
        is DatatypeType -> generateDatatypeInstantiation(context, targetType)
        is TopLevelDatatypeType -> generateTopLevelDatatypeInstantiation(context, targetType)
        is ArrayType -> generateArrayInitialisation(context, targetType)
        is MapType -> generateMapConstructor(context, targetType)
        is SetType, is MultisetType -> generateSetDisplay(context, targetType)
        is StringType -> generateStringLiteral(context)
        is SequenceType -> generateSequenceDisplay(context, targetType)
        else -> throw UnsupportedOperationException("Trying to generate constructor for non-constructor type")
    }

    private fun generateClassConstructor(
        context: GenerationContext,
        targetType: ClassType,
    ): Pair<ClassInstantiationAST, List<StatementAST>> {
        val clazz = targetType.clazz
        val (params, paramDeps) = clazz.constructorFields.map {
            generateExpression(context.increaseExpressionDepth(), it.type())
        }.foldPair()

        return Pair(ClassInstantiationAST(clazz, params), paramDeps)
    }

    private fun generateTraitConstructor(
        context: GenerationContext,
        targetType: TraitType,
    ): Pair<ClassInstantiationAST, List<StatementAST>> {
        val classes = context.functionSymbolTable.classes().filter { targetType.trait in it.extends }
        if (classes.isEmpty()) {
            generateClass(context, listOf(targetType.trait))
        }

        val selectedClass = selectionManager.randomSelection(
            context.functionSymbolTable.classes().filter { targetType.trait in it.extends },
        )

        return generateClassConstructor(context, ClassType(selectedClass))
    }

    override fun generateStringLiteral(context: GenerationContext): Pair<StringLiteralAST, List<StatementAST>> {
        val length = selectionManager.selectStringLength()
        val chars = (1..length).map { selectionManager.selectCharacter() }.toCharArray()
        return Pair(StringLiteralAST(chars.concatToString()), emptyList())
    }

    private fun generateLiteralForType(
        context: GenerationContext,
        targetType: LiteralType,
    ): Pair<LiteralAST, List<StatementAST>> = when (targetType) {
        BoolType -> generateBooleanLiteral(context)
        CharType -> generateCharLiteral(context)
        IntType -> generateIntegerLiteral(context)
        // should not be possible to reach here
        else -> throw UnsupportedOperationException("Trying to generate literal for non-literal type $targetType")
    }

    override fun generateIntegerLiteral(context: GenerationContext): Pair<IntegerLiteralAST, List<StatementAST>> {
        val isHex = selectionManager.randomWeightedSelection(listOf(true to 0.7, false to 0.3))
        val value = generateDecimalLiteralValue(negative = true)
        return Pair(IntegerLiteralAST(value, isHex), emptyList())
    }

    override fun generateArrayInitialisation(
        context: GenerationContext,
        targetType: ArrayType,
    ): Pair<ArrayInitAST, List<StatementAST>> =
        Pair(ArrayInitAST(selectionManager.selectArrayLength(), targetType), emptyList())

    override fun generateBooleanLiteral(context: GenerationContext): Pair<BooleanLiteralAST, List<StatementAST>> =
        Pair(BooleanLiteralAST(selectionManager.selectBoolean()), emptyList())

    override fun generateCharLiteral(context: GenerationContext): Pair<CharacterLiteralAST, List<StatementAST>> {
        val c = selectionManager.selectCharacter()
        return Pair(CharacterLiteralAST(c), emptyList())
    }

    override fun generateType(context: GenerationContext): Type =
        selectionManager.selectType(context)

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
        private const val DAFNY_MAX_LOOP_COUNTER = 100
        const val GLOBAL_STATE = "GlobalState"
        private const val PARAM_GLOBAL_STATE = "globalState"
    }
}
