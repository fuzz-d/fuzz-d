package fuzzd.recondition.visitor

import dafnyBaseVisitor
import dafnyParser.* // ktlint-disable no-wildcard-imports
import fuzzd.generator.Generator.Companion.GLOBAL_STATE
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.DatatypeAST
import fuzzd.generator.ast.DatatypeConstructorAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayInitAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeDestructorAST
import fuzzd.generator.ast.ExpressionAST.DatatypeInstantiationAST
import fuzzd.generator.ast.ExpressionAST.DatatypeUpdateAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
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
import fuzzd.generator.ast.ExpressionAST.ObjectOrientedInstanceAST
import fuzzd.generator.ast.ExpressionAST.SequenceComprehensionAST
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
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.MatchStatementAST
import fuzzd.generator.ast.StatementAST.MultiDeclarationAST
import fuzzd.generator.ast.StatementAST.MultiTypedDeclarationAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.VoidMethodCallAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
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
import fuzzd.generator.ast.Type.DataStructureType.MapType
import fuzzd.generator.ast.Type.MethodReturnType
import fuzzd.generator.ast.Type.DataStructureType.MultisetType
import fuzzd.generator.ast.Type.PlaceholderType
import fuzzd.generator.ast.Type.DataStructureType.SequenceType
import fuzzd.generator.ast.Type.DataStructureType.SetType
import fuzzd.generator.ast.Type.DataStructureType.StringType
import fuzzd.generator.ast.Type.TopLevelDatatypeType
import fuzzd.generator.ast.Type.TraitType
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
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_ARRAY_INDEX
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.toHexInt
import fuzzd.utils.unionAll

class DafnyVisitor : dafnyBaseVisitor<ASTElement>() {
    private val topLevelDatatypes = mutableListOf<DatatypeDeclContext>()
    private val topLevelClasses = mutableListOf<ClassDeclContext>()
    val topLevelTraits = mutableListOf<TraitDeclContext>()

    private val datatypesTable = VisitorSymbolTable<DatatypeAST>()
    private val classesTable = VisitorSymbolTable<ClassAST>()
    private val classFieldsTable = VisitorSymbolTable<IdentifierAST>()
    private val traitsTable = VisitorSymbolTable<TraitAST>()
    private var functionMethodsTable = VisitorSymbolTable<FunctionMethodSignatureAST>()
    private var methodsTable = VisitorSymbolTable<MethodSignatureAST>()
    private var identifiersTable = VisitorSymbolTable<IdentifierAST>()

    private fun addToTables(identifier: IdentifierAST, table: VisitorSymbolTable<IdentifierAST>) {
        table.addEntry(identifier.name, identifier)
        if (identifier is ObjectOrientedInstanceAST) {
            identifier.fields().forEach { f -> addToTables(f, table) }
            identifier.functionMethods().forEach { f -> functionMethodsTable.addEntry(f.name, f) }
            identifier.methods().forEach { m -> methodsTable.addEntry(m.name, m) }
        } else if (identifier is TopLevelDatatypeInstanceAST) {
            identifier.datatype.datatype.constructors.forEach {
                it.fields.forEach { f ->
                    if (f == identifier) {
                        table.addEntry(
                            identifier.name,
                            identifier,
                        )
                    } else {
                        addToTables(f, table)
                    }
                }
            }
        }
    }

    private fun increaseTablesDepth() {
        identifiersTable = identifiersTable.increaseDepth()
        functionMethodsTable = functionMethodsTable.increaseDepth()
        methodsTable = methodsTable.increaseDepth()
    }

    private fun decreaseTablesDepth() {
        methodsTable = methodsTable.decreaseDepth()
        functionMethodsTable = functionMethodsTable.decreaseDepth()
        identifiersTable = identifiersTable.decreaseDepth()
    }

    /* ============================================ TOP LEVEL ============================================ */
    override fun visitProgram(ctx: ProgramContext): DafnyAST {
        listOf(
            SAFE_ARRAY_INDEX,
            SAFE_DIVISION_INT,
            SAFE_MODULO_INT,
            ABSOLUTE,
        ).forEach { functionMethodsTable.addEntry(it.name(), it.signature) }

        topLevelDatatypes.addAll(ctx.topDecl().mapNotNull { it.datatypeDecl() })
        topLevelClasses.addAll(ctx.topDecl().mapNotNull { it.classDecl() })
        topLevelTraits.addAll(ctx.topDecl().mapNotNull { it.traitDecl() })

        val globalStateCtx = ctx.topDecl().first {
            it.classDecl() != null && visitUpperIdentifierName(it.classDecl().upperIdentifier(0)) == GLOBAL_STATE
        }

        visitClassDecl(globalStateCtx.classDecl())

        val advancedStateCtx = ctx.topDecl().filter {
            it.classDecl() != null && visitUpperIdentifierName(it.classDecl().upperIdentifier(0)) == ADVANCED_STATE
        }

        if (advancedStateCtx.isNotEmpty()) {
            visitClassDecl(advancedStateCtx[0].classDecl())
        }

        val topLevelContexts = ctx.topDecl().filter { it.topDeclMember() != null }.map { it.topDeclMember() }

        topLevelContexts.forEach { visitTopDeclMemberPrimaryPass(it) }

        val traits = ctx.topDecl().filter { it.traitDecl() != null }.map { visitTraitDecl(it.traitDecl()) }
        val classes = ctx.topDecl().filter { it.classDecl() != null }.map { visitClassDecl(it.classDecl()) }
        val topLevelMembers = topLevelContexts.map { visitTopDeclMember(it) }

        return DafnyAST(datatypesTable.values() + traits + classes + topLevelMembers)
    }

    private fun visitTopDeclMemberPrimaryPass(ctx: TopDeclMemberContext) {
        if (ctx.functionDecl() != null) {
            visitFunctionSignatureDecl(ctx.functionDecl().functionSignatureDecl())
        } else {
            visitMethodSignatureDecl(ctx.methodDecl().methodSignatureDecl())
        }
    }

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = super.visitTopDecl(ctx) as TopLevelAST

    private fun isInductiveConstructor(datatypeName: String, constructor: DatatypeConstructorContext): Boolean =
        constructor.parameters()?.identifierType()?.any { p ->
            p.type().upperIdentifier() != null && visitUpperIdentifierName(p.type().upperIdentifier()) == datatypeName
        } == true

    override fun visitDatatypeDecl(ctx: DatatypeDeclContext): DatatypeAST {
        val name = visitUpperIdentifierName(ctx.upperIdentifier())
        val isInductive = ctx.datatypeConstructor().any { isInductiveConstructor(name, it) }
        val constructors = ctx.datatypeConstructor()
            .filter { !isInductive || !isInductiveConstructor(name, it) }
            .map(this::visitDatatypeConstructor)
        val datatype = DatatypeAST(name, constructors.toMutableList())

        if (isInductive) {
            val inductiveConstructor = ctx.datatypeConstructor().first { isInductiveConstructor(name, it) }
            val inductiveConstructorName = visitUpperIdentifierName(inductiveConstructor.upperIdentifier())
            val inductiveConstructorFieldName = visitIdentifierName(inductiveConstructor.parameters().identifierType(0).identifier())

            datatype.constructors.add(
                DatatypeConstructorAST(
                    inductiveConstructorName,
                    listOf(TopLevelDatatypeInstanceAST(inductiveConstructorFieldName, TopLevelDatatypeType(datatype))),
                ),
            )
        }

        datatypesTable.addEntry(name, datatype)
        return datatype
    }

    override fun visitDatatypeConstructor(ctx: DatatypeConstructorContext): DatatypeConstructorAST {
        val name = visitUpperIdentifierName(ctx.upperIdentifier())
        val fields = if (ctx.parameters() != null) visitParametersList(ctx.parameters()) else emptyList()

        return DatatypeConstructorAST(name, fields.toMutableList())
    }

    override fun visitClassDecl(ctx: ClassDeclContext): ClassAST {
        val name = visitUpperIdentifierName(ctx.upperIdentifier(0))

        if (classesTable.hasEntry(name)) return classesTable.getEntry(name)

        val extends = ctx.upperIdentifier().subList(1, ctx.upperIdentifier().size)
            .map { identifierCtx -> visitUpperIdentifierName(identifierCtx) }
            .map { identifierStr -> visitTopLevelName(identifierStr) as TraitAST }.toSet()

        increaseTablesDepth()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodAST>()
        val methods = mutableSetOf<MethodAST>()

        // form top level view of class
        ctx.classMemberDecl().filter { it.functionDecl() != null }
            .forEach { visitFunctionSignatureDecl(it.functionDecl().functionSignatureDecl()) }

        ctx.classMemberDecl().filter { it.methodDecl() != null }
            .forEach { visitMethodSignatureDecl(it.methodDecl().methodSignatureDecl()) }

        ctx.classMemberDecl().forEach { classMemberDeclCtx ->
            when (val astNode = super.visitClassMemberDecl(classMemberDeclCtx)) {
                is IdentifierAST -> {
                    fields.add(astNode)
                    addToTables(astNode, classFieldsTable)
                }

                is FunctionMethodAST -> functionMethods.add(astNode)
                is MethodAST -> methods.add(astNode)
            }
        }

        val inheritedFields = extends.map { it.fields() }.unionAll()

        decreaseTablesDepth()

        val clazz = ClassAST(name, extends, functionMethods, methods, fields, inheritedFields)
        classesTable.addEntry(name, clazz)
        return clazz
    }

    override fun visitTraitDecl(ctx: TraitDeclContext): TraitAST {
        val name = visitUpperIdentifierName(ctx.upperIdentifier(0))
        if (traitsTable.hasEntry(name)) return traitsTable.getEntry(name)

        val extends = ctx.upperIdentifier().subList(1, ctx.upperIdentifier().size)
            .map { identifierCtx -> visitUpperIdentifierName(identifierCtx) }
            .map { identifierStr -> visitTopLevelName(identifierStr) as TraitAST }.toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        increaseTablesDepth()

        ctx.traitMemberDecl().forEach { traitMemberDeclCtx ->
            when (val astNode = super.visitTraitMemberDecl(traitMemberDeclCtx)) {
                is IdentifierAST -> {
                    fields.add(astNode)
                    addToTables(astNode, classFieldsTable)
                }

                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        decreaseTablesDepth()

        val trait = TraitAST(name, extends, functionMethods, methods, fields)
        traitsTable.addEntry(name, trait)
        return trait
    }

    override fun visitTopDeclMember(ctx: TopDeclMemberContext): TopLevelAST =
        super.visitTopDeclMember(ctx) as TopLevelAST

    override fun visitFieldDecl(ctx: FieldDeclContext): IdentifierAST = visitIdentifierType(ctx.identifierType())

    override fun visitFunctionDecl(ctx: FunctionDeclContext): FunctionMethodAST {
        val signature = visitFunctionSignatureDecl(ctx.functionSignatureDecl())

        val prevIdentifiersTable = identifiersTable
        identifiersTable = VisitorSymbolTable()
        signature.params.forEach { param -> identifiersTable.addEntry(param.name, param) }

        val body = visitExpression(ctx.expression())

        identifiersTable = prevIdentifiersTable
        functionMethodsTable.addEntry(signature.name, signature)
        return FunctionMethodAST(signature, body)
    }

    override fun visitFunctionSignatureDecl(ctx: FunctionSignatureDeclContext): FunctionMethodSignatureAST {
        val name = if (ctx.identifier() != null) {
            visitIdentifierName(ctx.identifier())
        } else {
            visitUpperIdentifierName(ctx.upperIdentifier())
        }

        if (functionMethodsTable.hasEntry(name)) {
            return functionMethodsTable.getEntry(name)
        }

        val params = visitParametersList(ctx.parameters())
        val returnType = visitType(ctx.type())

        val signature = FunctionMethodSignatureAST(name, returnType, params)
        functionMethodsTable.addEntry(name, signature)
        return signature
    }

    override fun visitMethodDecl(ctx: MethodDeclContext): ASTElement {
        val signature = visitMethodSignatureDecl(ctx.methodSignatureDecl())

        val prevIdentifierTable = identifiersTable
        identifiersTable = VisitorSymbolTable()

        signature.params.forEach { param -> identifiersTable.addEntry(param.name, param) }
        signature.returns.forEach { r -> identifiersTable.addEntry(r.name, r) }

        val body = visitSequence(ctx.sequence())
        val method = if (signature.name == "Main") MainFunctionAST(body) else MethodAST(signature, body)

        identifiersTable = prevIdentifierTable
        return method
    }

    override fun visitMethodSignatureDecl(ctx: MethodSignatureDeclContext): MethodSignatureAST {
        val name = if (ctx.identifier() != null) {
            visitIdentifierName(ctx.identifier())
        } else {
            visitUpperIdentifierName(ctx.upperIdentifier())
        }

        if (methodsTable.hasEntry(name)) {
            return methodsTable.getEntry(name)
        }

        val params = visitParametersList(ctx.parameters(0))
        val returns = if (ctx.parameters().size > 1) visitReturnsList(ctx.parameters(1)) else emptyList()

        val signature = MethodSignatureAST(name, params, returns)
        methodsTable.addEntry(name, signature)
        return signature
    }

    private fun visitReturnsList(ctx: ParametersContext): List<IdentifierAST> =
        ctx.identifierType().map { identifierTypeCtx -> visitIdentifierType(identifierTypeCtx) }.map { identifier ->
            IdentifierAST(
                identifier.name,
                identifier.type(),
                mutable = true,
                initialised = false,
            )
        }

    private fun visitParametersList(ctx: ParametersContext): List<IdentifierAST> =
        ctx.identifierType().map { identifierTypeCtx -> visitIdentifierType(identifierTypeCtx) }.map { identifier ->
            val result = when (val type = identifier.type()) {
                is ClassType -> ClassInstanceAST(type.clazz, identifier.name, mutable = false, initialised = true)
                is TraitType -> TraitInstanceAST(type.trait, identifier.name, mutable = false, initialised = true)
                is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(identifier.name, type, mutable = false, initialised = true)
                else -> IdentifierAST(identifier.name, identifier.type(), mutable = false, initialised = true)
            }

            addToTables(result, identifiersTable)
            result
        }

    override fun visitSequence(ctx: SequenceContext): SequenceAST =
        SequenceAST(ctx.statement().map(this::visitStatement))

    override fun visitIdentifierType(ctx: IdentifierTypeContext): IdentifierAST {
        val type = visitType(ctx.type())
        val name = visitIdentifierName(ctx.identifier())
        return when (type) {
            is ClassType -> ClassInstanceAST(type.clazz, name)
            is TraitType -> TraitInstanceAST(type.trait, name)
            is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(name, type)
            else -> IdentifierAST(name, type)
        }
    }

    /* ============================================ STATEMENTS ========================================= */

    override fun visitStatement(ctx: StatementContext): StatementAST = super.visitStatement(ctx) as StatementAST

    override fun visitBreakStatement(ctx: BreakStatementContext): BreakAST = BreakAST

    override fun visitDeclaration(ctx: DeclarationContext): StatementAST {
        val lhs = ctx.declarationLhs().declAssignLhs().map { declAssignLhs -> visitDeclAssignLhs(declAssignLhs) }
        val rhs = visitDeclAssignRhs(ctx.declAssignRhs())

        val rhsTypes = if (ctx.type() != null) {
            listOf(visitType(ctx.type()))
        } else if (rhs.type() is MethodReturnType) {
            (rhs.type() as MethodReturnType).types
        } else {
            listOf(rhs.type())
        }

        val identifiers = lhs.indices.map { i ->
            val type = rhsTypes[i]
            val identifier = when (type) {
                is ClassType -> ClassInstanceAST(type.clazz, lhs[i].name)
                is TraitType -> TraitInstanceAST(type.trait, lhs[i].name)
                is TopLevelDatatypeType -> TopLevelDatatypeInstanceAST(lhs[i].name, type, mutable = true, initialised = true)
                else -> IdentifierAST(lhs[i].name, type, mutable = true, initialised = true)
            }

            addToTables(identifier, identifiersTable)

            identifier
        }

        return if (ctx.type() != null) {
            MultiTypedDeclarationAST(identifiers, listOf(rhs))
        } else {
            MultiDeclarationAST(identifiers, listOf(rhs))
        }
    }

    private fun visitDeclAssignLhsMethodCall(ctx: DeclAssignLhsContext): String {
        val strings = mutableListOf<String>()
        var currCtx: DeclAssignLhsContext? = ctx

        while (currCtx != null) {
            val str = visitDeclIdentifier(currCtx.declIdentifier()).toString()

            strings.add(str)
            currCtx = currCtx.declAssignLhs()
        }

        return strings.joinToString(".")
    }

    override fun visitDeclIdentifier(ctx: DeclIdentifierContext): IdentifierAST {
        val identifier = visitIdentifier(ctx.identifier())
        return if (ctx.expression().size == 0) {
            identifier
        } else {
            val index = visitExpression(ctx.expression(0))
            ArrayIndexAST(identifier, index)
        }
    }

    override fun visitDeclAssignLhs(ctx: DeclAssignLhsContext): IdentifierAST {
        val identifier = visitDeclIdentifier(ctx.declIdentifier())

        return if (ctx.declAssignLhs() != null) {
            ClassInstanceFieldAST(identifier, visitDeclAssignLhs(ctx.declAssignLhs()))
        } else {
            identifier
        }
    }

    override fun visitDeclAssignRhs(ctx: DeclAssignRhsContext): ExpressionAST =
        super.visitDeclAssignRhs(ctx) as ExpressionAST

    override fun visitAssignment(ctx: AssignmentContext): AssignmentAST {
        val lhs = visitAssignmentLhs(ctx.assignmentLhs())
        val rhs = visitDeclAssignRhs(ctx.declAssignRhs())

        return AssignmentAST(lhs, rhs)
    }

    override fun visitAssignmentLhs(ctx: AssignmentLhsContext): IdentifierAST = visitDeclAssignLhs(ctx.declAssignLhs())

    override fun visitPrint(ctx: PrintContext): PrintAST {
        val exprs = ctx.expression().map(this::visitExpression)
        return if (exprs.last() == StringLiteralAST("\\n")) {
            PrintAST(exprs.subList(0, exprs.size - 1), true)
        } else {
            PrintAST(exprs, false)
        }
    }

    override fun visitMatchStatement(ctx: MatchStatementContext): MatchStatementAST {
        val match = visitExpression(ctx.expression())
        val cases = ctx.caseStatement().map(this::visitMatchStatementCase)

        return MatchStatementAST(match, cases)
    }

    private fun visitMatchStatementCase(ctx: CaseStatementContext): Pair<ExpressionAST, SequenceAST> {
        val expr = visitExpression(ctx.expression())
        val exprType = expr.type() as DatatypeType

        increaseTablesDepth()
        exprType.constructor.fields.forEach { addToTables(it, identifiersTable) }
        val seq = visitSequence(ctx.sequence())
        decreaseTablesDepth()

        return Pair(expr, seq)
    }

    override fun visitIfStatement(ctx: IfStatementContext): IfStatementAST {
        val ifCondition = visitExpression(ctx.expression())

        identifiersTable = identifiersTable.increaseDepth()
        val ifBranch = visitSequence(ctx.sequence(0))
        identifiersTable = identifiersTable.decreaseDepth()

        identifiersTable = identifiersTable.increaseDepth()
        val elseBranch = if (ctx.sequence().size > 1) visitSequence(ctx.sequence(1)) else null
        identifiersTable = identifiersTable.decreaseDepth()

        return IfStatementAST(ifCondition, ifBranch, elseBranch)
    }

    override fun visitWhileStatement(ctx: WhileStatementContext): WhileLoopAST {
        val whileCondition = visitExpression(ctx.expression())

        identifiersTable = identifiersTable.increaseDepth()
        val sequence = visitSequence(ctx.sequence())
        identifiersTable = identifiersTable.decreaseDepth()

        return WhileLoopAST(whileCondition, sequence)
    }

    override fun visitVoidMethodCall(ctx: VoidMethodCallContext): VoidMethodCallAST {
        val ident = visitDeclAssignLhs(ctx.declAssignLhs())
        val method = methodsTable.getEntry(ident.name)
        val params = visitParametersForCall(ctx.callParameters())

        return VoidMethodCallAST(method, params)
    }

    /* ============================================= EXPRESSION ======================================== */

    override fun visitExpression(ctx: ExpressionContext): ExpressionAST =
        when {
            ctx.literal() != null -> visitLiteral(ctx.literal())
            ctx.functionCall() != null -> visitFunctionCall(ctx.functionCall())
            ctx.unaryOperator() != null -> UnaryExpressionAST(
                visitExpression(ctx.expression(0)),
                visitUnaryOperator(ctx.unaryOperator()),
            )

            ctx.modulus() != null -> visitModulus(ctx.modulus())
            ctx.multisetConversion() != null -> visitMultisetConversion(ctx.multisetConversion())
            ctx.classInstantiation() != null -> visitClassInstantiation(ctx.classInstantiation())
            ctx.datatypeInstantiation() != null -> visitDatatypeInstantiation(ctx.datatypeInstantiation())
            ctx.ternaryExpression() != null -> visitTernaryExpression(ctx.ternaryExpression())
            ctx.matchExpression() != null -> visitMatchExpression(ctx.matchExpression())
            ctx.arrayLength() != null -> visitArrayLength(ctx.arrayLength())
            ctx.setDisplay() != null -> visitSetDisplay(ctx.setDisplay())
            ctx.sequenceDisplay() != null -> visitSequenceDisplay(ctx.sequenceDisplay())
            ctx.sequenceComprehension() != null -> visitSequenceComprehension(ctx.sequenceComprehension())
            ctx.mapConstructor() != null -> visitMapConstructor(ctx.mapConstructor())
            ctx.identifier() != null -> visitIdentifier(ctx.identifier())
            ctx.index() != null -> visitIndexExpression(ctx)
            ctx.indexElem() != null -> visitIndexAssign(ctx)
            ctx.datatypeFieldUpdate().isNotEmpty() -> visitDatatypeUpdate(ctx)
            ctx.DOT() != null -> visitDotExpression(ctx)

            ctx.ADD() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(expr1, if (expr1.type() is LiteralType) AdditionOperator else UnionOperator, expr2)
            }

            ctx.AND() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ConjunctionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.disj() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                DisjointOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.DIV() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                DivisionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.EQ() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) EqualsOperator else DataStructureEqualityOperator,
                    expr2,
                )
            }

            ctx.GEQ() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) GreaterThanEqualOperator else SupersetOperator,
                    expr2,
                )
            }

            ctx.GT() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) GreaterThanOperator else ProperSupersetOperator,
                    expr2,
                )
            }

            ctx.IFF() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                IffOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.IMP() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ImplicationOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.IN() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                MembershipOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.LEQ() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) LessThanEqualOperator else SubsetOperator,
                    expr2,
                )
            }

            ctx.LT() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) LessThanOperator else ProperSubsetOperator,
                    expr2,
                )
            }

            ctx.MOD() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ModuloOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.MUL() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) MultiplicationOperator else IntersectionOperator,
                    expr2,
                )
            }

            ctx.NEG() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) SubtractionOperator else DifferenceOperator,
                    expr2,
                )
            }

            ctx.NEQ() != null -> {
                val expr1 = visitExpression(ctx.expression(0))
                val expr2 = visitExpression(ctx.expression(1))
                BinaryExpressionAST(
                    expr1,
                    if (expr1.type() is LiteralType) NotEqualsOperator else DataStructureInequalityOperator,
                    expr2,
                )
            }

            ctx.NOT_IN() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                AntiMembershipOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.OR() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                DisjunctionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.RIMP() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ReverseImplicationOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.expression() != null -> visitExpression(ctx.expression(0))

            else -> throw UnsupportedOperationException("No valid expression types found")
        }

    override fun visitClassInstantiation(ctx: ClassInstantiationContext): ClassInstantiationAST {
        val className = visitUpperIdentifierName(ctx.upperIdentifier())
        val callParams = visitParametersForCall(ctx.callParameters())

        if (!classesTable.hasEntry(className)) {
            throw UnsupportedOperationException("Visiting instantiation for unknown class $className")
        }

        return ClassInstantiationAST(classesTable.getEntry(className), callParams)
    }

    override fun visitDatatypeInstantiation(ctx: DatatypeInstantiationContext): DatatypeInstantiationAST {
        val datatypeConstructorName = visitUpperIdentifierName(ctx.upperIdentifier())
        val datatype = visitDatatypeFromConstructor(datatypeConstructorName)
        val constructor = datatype.constructors.first { it.name == datatypeConstructorName }
        val params = visitParametersForCall(ctx.callParameters())

        return DatatypeInstantiationAST(datatype, constructor, params)
    }

    private fun visitDatatypeUpdate(ctx: ExpressionContext): DatatypeUpdateAST {
        val datatypeInstance = visitExpression(ctx.expression(0))
        val datatypeType = datatypeInstance.type() as TopLevelDatatypeType

        identifiersTable = identifiersTable.increaseDepth()
        datatypeType.datatype.constructors.forEach { c ->
            c.fields.forEach { identifiersTable.addEntry(it.name, it) }
        }
        val updates = ctx.datatypeFieldUpdate().map(this::visitDatatypeFieldUpdatePair)
        identifiersTable = identifiersTable.decreaseDepth()

        return DatatypeUpdateAST(datatypeInstance, updates)
    }

    private fun visitDatatypeFieldUpdatePair(ctx: DatatypeFieldUpdateContext): Pair<IdentifierAST, ExpressionAST> =
        Pair(visitIdentifier(ctx.identifier()), visitExpression(ctx.expression()))

    override fun visitTernaryExpression(ctx: TernaryExpressionContext): TernaryExpressionAST {
        val visitedExprs = ctx.expression().map { expr -> visitExpression(expr) }
        return TernaryExpressionAST(visitedExprs[0], visitedExprs[1], visitedExprs[2])
    }

    override fun visitMatchExpression(ctx: MatchExpressionContext): MatchExpressionAST {
        val match = visitExpression(ctx.expression())
        val cases = ctx.caseExpression().map(this::visitMatchExpressionCase)
        val type = cases[0].second.type()

        return MatchExpressionAST(match, type, cases)
    }

    private fun visitMatchExpressionCase(ctx: CaseExpressionContext): Pair<ExpressionAST, ExpressionAST> {
        val case = visitExpression(ctx.expression(0))
        val caseType = case.type() as DatatypeType

        increaseTablesDepth()
        caseType.constructor.fields.forEach { addToTables(it, identifiersTable) }
        val expression = visitExpression(ctx.expression(1))
        decreaseTablesDepth()

        return Pair(case, expression)
    }

    override fun visitArrayConstructor(ctx: ArrayConstructorContext): ArrayInitAST {
        val innerType = visitType(ctx.type())
        val dimension = visitIntLiteral(ctx.intLiteral(0)).toString().toInt()

        return ArrayInitAST(dimension, ArrayType(innerType))
    }

    private fun visitDotExpression(ctx: ExpressionContext): ExpressionAST {
        val beforeDot = visitExpression(ctx.expression(0))

        return when (val type = beforeDot.type()) {
            is ClassType -> {
                increaseTablesDepth()
                type.clazz.fields.forEach { addToTables(it, identifiersTable) }
                type.clazz.functionMethods.forEach { functionMethodsTable.addEntry(it.name(), it.signature) }
                type.clazz.methods.forEach { methodsTable.addEntry(it.name(), it.signature) }
                val afterDot = visitExpression(ctx.expression(1)) as IdentifierAST
                decreaseTablesDepth()
                ClassInstanceFieldAST(beforeDot as IdentifierAST, afterDot)
            }

            is TraitType -> {
                increaseTablesDepth()
                type.trait.fields().forEach { addToTables(it, identifiersTable) }
                type.trait.functionMethods().forEach { functionMethodsTable.addEntry(it.name, it) }
                type.trait.methods().forEach { methodsTable.addEntry(it.name, it) }
                val afterDot = visitExpression(ctx.expression(1)) as IdentifierAST
                decreaseTablesDepth()
                ClassInstanceFieldAST(beforeDot as IdentifierAST, afterDot)
            }

            is TopLevelDatatypeType -> {
                increaseTablesDepth()
                type.datatype.constructors.forEach { c ->
                    c.fields.forEach { addToTables(it, identifiersTable) }
                }
                val afterDot = visitExpression(ctx.expression(1)) as IdentifierAST
                decreaseTablesDepth()
                DatatypeDestructorAST(beforeDot, afterDot)
            }

            else -> {
                throw UnsupportedOperationException()
            }
        }
    }

    override fun visitArrayLength(ctx: ArrayLengthContext): ArrayLengthAST =
        ArrayLengthAST(visitDeclAssignLhs(ctx.declAssignLhs()))

    override fun visitFunctionCall(ctx: FunctionCallContext): ExpressionAST {
        val methodName = visitDeclAssignLhsMethodCall(ctx.declAssignLhs())
        val callParameters = visitParametersForCall(ctx.callParameters())

        return if (methodsTable.hasEntry(methodName)) {
            val method = methodsTable.getEntry(methodName)
            NonVoidMethodCallAST(method, callParameters)
        } else {
            val functionMethod = functionMethodsTable.getEntry(methodName)
            FunctionMethodCallAST(functionMethod, callParameters)
        }
    }

    override fun visitSetDisplay(ctx: SetDisplayContext): SetDisplayAST {
        val exprs = ctx.expression().map(this::visitExpression)
        return SetDisplayAST(exprs, ctx.MULTISET() != null)
    }

    private fun visitIndexExpression(ctx: ExpressionContext): ExpressionAST {
        val expression = visitExpression(ctx.expression(0))
        val index = visitExpression(ctx.index().expression(0))
        return when (expression.type()) {
            is ArrayType -> ArrayIndexAST(expression as IdentifierAST, index)
            is MapType -> MapIndexAST(expression, index)
            is MultisetType -> MultisetIndexAST(expression, index)
            else -> SequenceIndexAST(expression, index)
        }
    }

    override fun visitSequenceDisplay(ctx: SequenceDisplayContext): SequenceDisplayAST {
        val exprs = ctx.expression().map(this::visitExpression)
        return SequenceDisplayAST(exprs)
    }

    override fun visitSequenceComprehension(ctx: SequenceComprehensionContext): SequenceComprehensionAST {
        val size = visitExpression(ctx.expression(0))
        val identifierName = visitIdentifierName(ctx.identifier())
        val identifier = IdentifierAST(identifierName, IntType)

        identifiersTable = identifiersTable.increaseDepth()
        identifiersTable.addEntry(identifierName, identifier)
        val expr = visitExpression(ctx.expression(1))
        identifiersTable = identifiersTable.decreaseDepth()

        return SequenceComprehensionAST(size, identifier, expr)
    }

    override fun visitMapConstructor(ctx: MapConstructorContext): MapConstructorAST {
        val assigns = ctx.indexElem().map(this::visitIndexElement)
        val keyType = assigns[0].first.type()
        val valueType = assigns[0].second.type()
        return MapConstructorAST(keyType, valueType, assigns)
    }

    private fun visitIndexElement(ctx: IndexElemContext): Pair<ExpressionAST, ExpressionAST> = Pair(
        visitExpression(ctx.expression(0)),
        visitExpression(ctx.expression(1)),
    )

    private fun visitParametersForCall(ctx: CallParametersContext): List<ExpressionAST> =
        ctx.expression().map { expr -> visitExpression(expr) }

    override fun visitIdentifier(ctx: IdentifierContext): IdentifierAST {
        val name = visitIdentifierName(ctx)
        return findIdentifier(name) // return actual identifier or one with dummy type
    }

    private fun visitIndexAssign(ctx: ExpressionContext): IndexAssignAST {
        val expression = visitExpression(ctx.expression(0))
        val assign = visitIndexElement(ctx.indexElem())

        return IndexAssignAST(expression, assign.first, assign.second)
    }

    private fun findIdentifier(name: String): IdentifierAST =
        if (identifiersTable.hasEntry(name)) {
            identifiersTable.getEntry(name)
        } else if (classFieldsTable.hasEntry(name)) {
            classFieldsTable.getEntry(name)
        } else {
            IdentifierAST(name, PlaceholderType)
        }

    override fun visitLiteral(ctx: LiteralContext) = super.visitLiteral(ctx) as LiteralAST

    override fun visitBoolLiteral(ctx: BoolLiteralContext): BooleanLiteralAST =
        BooleanLiteralAST(ctx.BOOL_LITERAL().toString().toBoolean())

    override fun visitIntLiteral(ctx: IntLiteralContext): IntegerLiteralAST =
        IntegerLiteralAST(ctx.INT_LITERAL().toString().toHexInt())

    override fun visitCharLiteral(ctx: CharLiteralContext): CharacterLiteralAST =
        CharacterLiteralAST(ctx.CHAR_LITERAL().toString()[1])

    override fun visitStringToken(ctx: StringTokenContext): StringLiteralAST =
        StringLiteralAST(ctx.text.substring(1, ctx.text.length - 1))

    override fun visitUnaryOperator(ctx: UnaryOperatorContext): UnaryOperator = when {
        ctx.NEG() != null -> NegationOperator
        ctx.NOT() != null -> NotOperator
        else -> throw UnsupportedOperationException("Visiting unsupported unary operator $ctx")
    }

    override fun visitModulus(ctx: ModulusContext): ModulusExpressionAST =
        ModulusExpressionAST(visitExpression(ctx.expression()))

    override fun visitMultisetConversion(ctx: MultisetConversionContext): MultisetConversionAST =
        MultisetConversionAST(visitExpression(ctx.expression()))

    /* ============================================== TYPE ============================================= */

    override fun visitType(ctx: TypeContext): Type = when {
        ctx.BOOL() != null -> BoolType
        ctx.INT() != null -> IntType
        ctx.CHAR() != null -> CharType
        ctx.STRING() != null -> StringType
        ctx.arrayType() != null -> visitArrayType(ctx.arrayType())
        ctx.upperIdentifier() != null -> visitUpperIdentifierType(ctx.upperIdentifier())
        ctx.mapType() != null -> visitMapType(ctx.mapType())
        ctx.setType() != null -> visitSetType(ctx.setType())
        ctx.multisetType() != null -> visitMultisetType(ctx.multisetType())
        ctx.sequenceType() != null -> visitSequenceType(ctx.sequenceType())
        else -> throw UnsupportedOperationException("Visiting unrecognised type context $ctx")
    }

    override fun visitArrayType(ctx: ArrayTypeContext): ArrayType {
        val innerType = visitType(ctx.genericInstantiation().type(0))
        return ArrayType(innerType)
    }

    private fun visitDatatypeFromConstructor(constructorName: String): DatatypeAST =
        datatypesTable.values().firstOrNull { d ->
            d.constructors.map { it.name }.contains(constructorName)
        } ?: run {
            val decl = topLevelDatatypes.first { d ->
                d.datatypeConstructor().any { visitUpperIdentifierName(it.upperIdentifier()) == constructorName }
            }

            visitDatatypeDecl(decl)
        }

    private fun visitTopLevelName(name: String): TopLevelAST {
        val classMatch = topLevelClasses.firstOrNull { visitUpperIdentifierName(it.upperIdentifier(0)) == name }
        val traitMatch = topLevelTraits.firstOrNull { visitUpperIdentifierName(it.upperIdentifier(0)) == name }
        val datatypeMatch = topLevelDatatypes.firstOrNull { visitUpperIdentifierName(it.upperIdentifier()) == name }

        return when {
            classMatch != null -> visitClassDecl(classMatch)
            traitMatch != null -> visitTraitDecl(traitMatch)
            datatypeMatch != null -> visitDatatypeDecl(datatypeMatch)
            else -> throw UnsupportedOperationException()
        }
    }

    fun visitUpperIdentifierType(ctx: UpperIdentifierContext): Type {
        val name = visitUpperIdentifierName(ctx)
        return when {
            classesTable.hasEntry(name) -> ClassType(classesTable.getEntry(name))
            traitsTable.hasEntry(name) -> TraitType(traitsTable.getEntry(name))
            datatypesTable.hasEntry(name) -> TopLevelDatatypeType(datatypesTable.getEntry(name))
            else -> {
                visitTopLevelName(name)
                visitUpperIdentifierType(ctx)
            }
        }
    }

    override fun visitMapType(ctx: MapTypeContext): MapType {
        val keyType = visitType(ctx.genericInstantiation().type(0))
        val valueType = visitType(ctx.genericInstantiation().type(1))

        return MapType(keyType, valueType)
    }

    override fun visitSetType(ctx: SetTypeContext): SetType {
        val innerType = visitType(ctx.genericInstantiation().type(0))
        return SetType(innerType)
    }

    override fun visitMultisetType(ctx: MultisetTypeContext): MultisetType {
        val innerType = visitType(ctx.genericInstantiation().type(0))
        return MultisetType(innerType)
    }

    override fun visitSequenceType(ctx: SequenceTypeContext): SequenceType {
        val innerType = visitType(ctx.genericInstantiation().type(0))
        return SequenceType(innerType)
    }

    companion object {
        const val ADVANCED_STATE = "AdvancedReconditionState"
    }
}
