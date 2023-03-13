package fuzzd.recondition

import dafnyBaseVisitor
import dafnyParser.ArrayConstructorContext
import dafnyParser.ArrayTypeContext
import dafnyParser.AssignmentContext
import dafnyParser.AssignmentLhsContext
import dafnyParser.BoolLiteralContext
import dafnyParser.BreakStatementContext
import dafnyParser.CallParametersContext
import dafnyParser.CharLiteralContext
import dafnyParser.ClassDeclContext
import dafnyParser.DeclAssignLhsContext
import dafnyParser.DeclAssignRhsContext
import dafnyParser.DeclarationContext
import dafnyParser.DeclarationLhsContext
import dafnyParser.ExpressionContext
import dafnyParser.FieldDeclContext
import dafnyParser.FunctionCallContext
import dafnyParser.FunctionDeclContext
import dafnyParser.FunctionSignatureDeclContext
import dafnyParser.IdentifierContext
import dafnyParser.IdentifierTypeContext
import dafnyParser.IfStatementContext
import dafnyParser.IntLiteralContext
import dafnyParser.LiteralContext
import dafnyParser.MethodDeclContext
import dafnyParser.MethodSignatureDeclContext
import dafnyParser.ParametersContext
import dafnyParser.PrintContext
import dafnyParser.ProgramContext
import dafnyParser.RealLiteralContext
import dafnyParser.SequenceContext
import dafnyParser.StatementContext
import dafnyParser.TopDeclContext
import dafnyParser.TopDeclMemberContext
import dafnyParser.TraitDeclContext
import dafnyParser.TypeContext
import dafnyParser.UnaryOperatorContext
import dafnyParser.WhileStatementContext
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
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
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.DeclarationAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.StatementAST.WhileLoopAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.PlaceholderType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.IffOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.ReverseImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import fuzzd.generator.ast.operators.UnaryOperator
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.utils.unionAll

class VisitorSymbolTable<T>(val parent: VisitorSymbolTable<T>? = null) {
    private val table = mutableMapOf<String, T>()

    fun addEntry(name: String, entry: T) {
        table[name] = entry
    }

    fun hasEntry(name: String): Boolean = table[name] != null || (parent?.hasEntry(name) ?: false)

    fun getEntry(name: String): T {
        if (!hasEntry(name)) throw UnsupportedOperationException("Visitor symbol table for entry {$name} not found")

        return if (table[name] != null) table[name]!! else parent!!.getEntry(name)
    }

    fun clone(): VisitorSymbolTable<T> {
        val cloned = VisitorSymbolTable(parent)

        table.entries.forEach { (name, entry) -> cloned.addEntry(name, entry) }

        return cloned
    }

    fun increaseDepth(): VisitorSymbolTable<T> = VisitorSymbolTable(this)

    fun decreaseDepth(): VisitorSymbolTable<T> {
        if (parent == null) throw UnsupportedOperationException("Can't decrease top level depth")
        return parent
    }
}

class DafnyVisitor : dafnyBaseVisitor<ASTElement>() {
    private val traitsTable = VisitorSymbolTable<TraitAST>()
    private var classesTable = VisitorSymbolTable<ClassAST>()
    private val classFieldsTable = VisitorSymbolTable<IdentifierAST>()

    private var functionMethodsTable = VisitorSymbolTable<FunctionMethodAST>()
    private var methodsTable = VisitorSymbolTable<MethodAST>()
    private var identifiersTable = VisitorSymbolTable<IdentifierAST>()

    /* ============================================ TOP LEVEL ============================================ */
    override fun visitProgram(ctx: ProgramContext): DafnyAST =
        DafnyAST(
            (ctx.topDecl()?.map { topDeclCtx -> visitTopDecl(topDeclCtx) }) ?: listOf(),
        )

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = super.visitTopDecl(ctx) as TopLevelAST

    override fun visitClassDecl(ctx: ClassDeclContext): ClassAST {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx ->
                identifierCtx.IDENTIFIER().toString()
            }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }
            .toSet()

        functionMethodsTable = functionMethodsTable.increaseDepth()
        methodsTable = methodsTable.increaseDepth()
        identifiersTable = identifiersTable.increaseDepth()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodAST>()
        val methods = mutableSetOf<MethodAST>()

        ctx.classMemberDecl().forEach { classMemberDeclCtx ->
            when (val astNode = super.visitClassMemberDecl(classMemberDeclCtx)) {
                is IdentifierAST -> {
                    fields.add(astNode); classFieldsTable.addEntry(astNode.name, astNode)
                }

                is FunctionMethodAST -> functionMethods.add(astNode)
                is MethodAST -> methods.add(astNode)
            }
        }

        val inheritedFields = extends.map { it.fields() }.unionAll()

        functionMethodsTable = functionMethodsTable.decreaseDepth()
        methodsTable = methodsTable.decreaseDepth()
        identifiersTable = identifiersTable.decreaseDepth()

        val clazz = ClassAST(name, extends, functionMethods, methods, fields, inheritedFields)
        classesTable.addEntry(name, clazz)
        return clazz
    }

    override fun visitTraitDecl(ctx: TraitDeclContext): TraitAST {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx ->
                identifierCtx.IDENTIFIER().toString()
            }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }
            .toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        identifiersTable = identifiersTable.increaseDepth()

        ctx.traitMemberDecl().forEach { traitMemberDeclCtx ->
            when (val astNode = super.visitTraitMemberDecl(traitMemberDeclCtx)) {
                is IdentifierAST -> fields.add(astNode)
                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        identifiersTable = identifiersTable.decreaseDepth()

        val trait = TraitAST(name, extends, functionMethods, methods, fields)
        traitsTable.addEntry(name, trait)
        return trait
    }

    override fun visitTopDeclMember(ctx: TopDeclMemberContext): TopLevelAST =
        super.visitTopDeclMember(ctx) as TopLevelAST

    override fun visitFunctionSignatureDecl(ctx: FunctionSignatureDeclContext): FunctionMethodSignatureAST {
        val name = visitIdentifierName(ctx.identifier())

        val params = visitParametersList(ctx.parameters())
        val returnType = visitType(ctx.type())

        return FunctionMethodSignatureAST(name, returnType, params)
    }

    override fun visitFieldDecl(ctx: FieldDeclContext): IdentifierAST = visitIdentifierType(ctx.identifierType())

    override fun visitFunctionDecl(ctx: FunctionDeclContext): FunctionMethodAST {
        val signature = visitFunctionSignatureDecl(ctx.functionSignatureDecl())

        val prevIdentifiersTable = identifiersTable
        identifiersTable = VisitorSymbolTable()
        signature.params.forEach { param -> identifiersTable.addEntry(param.name, param) }

        val body = visitExpression(ctx.expression())
        val fm = FunctionMethodAST(signature, body)

        identifiersTable = prevIdentifiersTable
        functionMethodsTable.addEntry(signature.name, fm)
        return fm
    }

    override fun visitMethodDecl(ctx: MethodDeclContext): ASTElement {
        val signature = visitMethodSignatureDecl(ctx.methodSignatureDecl())

        val prevIdentifierTable = identifiersTable
        identifiersTable = VisitorSymbolTable()

        signature.params.forEach { param -> identifiersTable.addEntry(param.name, param) }
        signature.returns.forEach { r -> identifiersTable.addEntry(r.name, r) }

        val body = visitSequence(ctx.sequence())
        val method = MethodAST(signature)
        method.setBody(body)

        identifiersTable = prevIdentifierTable
        methodsTable.addEntry(signature.name, method)
        return method
    }

    override fun visitSequence(ctx: SequenceContext): SequenceAST =
        SequenceAST(ctx.statement().map(this::visitStatement))

    override fun visitMethodSignatureDecl(ctx: MethodSignatureDeclContext): MethodSignatureAST {
        val name = visitIdentifierName(ctx.identifier())

        val params = visitParametersList(ctx.parameters(0))
        val returns = if (ctx.parameters().size > 1) visitReturnsList(ctx.parameters(1)) else emptyList()

        return MethodSignatureAST(name, params, returns)
    }

    private fun visitReturnsList(ctx: ParametersContext): List<IdentifierAST> =
        ctx.identifierType()
            .map { identifierTypeCtx -> visitIdentifierType(identifierTypeCtx) }
            .map { identifier ->
                IdentifierAST(
                    identifier.name,
                    identifier.type(),
                    mutable = true,
                    initialised = false,
                )
            }

    private fun visitParametersList(ctx: ParametersContext): List<IdentifierAST> =
        ctx.identifierType()
            .map { identifierTypeCtx -> visitIdentifierType(identifierTypeCtx) }
            .map { identifier ->
                IdentifierAST(
                    identifier.name,
                    identifier.type(),
                    mutable = false,
                    initialised = true,
                )
            }

    override fun visitIdentifierType(ctx: IdentifierTypeContext): IdentifierAST =
        IdentifierAST(visitIdentifierName(ctx.identifier()), visitType(ctx.type()))

    /* ============================================ STATEMENTS ========================================= */

    override fun visitStatement(ctx: StatementContext): StatementAST = super.visitStatement(ctx) as StatementAST

    override fun visitBreakStatement(ctx: BreakStatementContext): BreakAST = BreakAST

    override fun visitDeclaration(ctx: DeclarationContext): DeclarationAST {
        val lhs = visitDeclarationLhs(ctx.declarationLhs())
        val rhs = visitDeclAssignRhs(ctx.declAssignRhs())

        val identifier = IdentifierAST(lhs.name, rhs.type(), mutable = true, initialised = true)
        identifiersTable.addEntry(identifier.name, identifier)
        return DeclarationAST(identifier, rhs)
    }

    override fun visitDeclarationLhs(ctx: DeclarationLhsContext): IdentifierAST =
        visitDeclAssignLhs(ctx.declAssignLhs())

    override fun visitDeclAssignLhs(ctx: DeclAssignLhsContext): IdentifierAST =
        super.visitDeclAssignLhs(ctx) as IdentifierAST

    override fun visitDeclAssignRhs(ctx: DeclAssignRhsContext): ExpressionAST =
        super.visitDeclAssignRhs(ctx) as ExpressionAST

    override fun visitAssignment(ctx: AssignmentContext): AssignmentAST {
        val lhs = visitAssignmentLhs(ctx.assignmentLhs())
        val rhs = visitDeclAssignRhs(ctx.declAssignRhs())

        return AssignmentAST(lhs, rhs)
    }

    override fun visitAssignmentLhs(ctx: AssignmentLhsContext): IdentifierAST =
        visitDeclAssignLhs(ctx.declAssignLhs()) as IdentifierAST

    override fun visitPrint(ctx: PrintContext): PrintAST = PrintAST(visitExpression(ctx.expression()))

    override fun visitIfStatement(ctx: IfStatementContext): IfStatementAST {
        val ifCondition = visitExpression(ctx.expression())
        val ifBranch = visitSequence(ctx.sequence(0))

        val elseBranch = if (ctx.sequence().size > 1) visitSequence(ctx.sequence(1)) else null

        return IfStatementAST(ifCondition, ifBranch, elseBranch)
    }

    override fun visitWhileStatement(ctx: WhileStatementContext): WhileLoopAST {
        val whileCondition = visitExpression(ctx.expression())
        val sequence = visitSequence(ctx.sequence())

        return WhileLoopAST(whileCondition, sequence)
    }

    private fun visitIdentifierName(identifierCtx: IdentifierContext): String = identifierCtx.IDENTIFIER().toString()

    /* ============================================= EXPRESSION ======================================== */

    override fun visitExpression(ctx: ExpressionContext): ExpressionAST =
        when {
            ctx.literal() != null -> visitLiteral(ctx.literal())
            ctx.functionCall() != null -> visitFunctionCall(ctx.functionCall())
            ctx.declAssignLhs() != null -> visitDeclAssignLhs(ctx.declAssignLhs())
            ctx.unaryOperator() != null -> UnaryExpressionAST(
                visitExpression(ctx.expression(0)),
                visitUnaryOperator(ctx.unaryOperator()),
            )

            ctx.ADD() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                AdditionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.AND() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ConjunctionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.DIV() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                DivisionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.EQ() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                EqualsOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.GEQ() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                GreaterThanEqualOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.GT() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                GreaterThanOperator,
                visitExpression(ctx.expression(1)),
            )

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

            ctx.LEQ() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                LessThanEqualOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.LT() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                LessThanOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.MOD() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                ModuloOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.MUL() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                MultiplicationOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.NEG() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                SubtractionOperator,
                visitExpression(ctx.expression(1)),
            )

            ctx.NEQ() != null -> BinaryExpressionAST(
                visitExpression(ctx.expression(0)),
                NotEqualsOperator,
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

    override fun visitArrayConstructor(ctx: ArrayConstructorContext): ArrayInitAST {
        val innerType = visitType(ctx.type())
        val dimension = visitIntLiteral(ctx.intLiteral(0)).toString().toInt()

        return ArrayInitAST(dimension, ArrayType(innerType))
    }

    override fun visitFunctionCall(ctx: FunctionCallContext): ExpressionAST {
        val name = visitIdentifierName(ctx.identifier())
        val callParameters = visitParametersForCall(ctx.callParameters())

        return if (methodsTable.hasEntry(name)) {
            val method = methodsTable.getEntry(name)
            NonVoidMethodCallAST(method.signature, callParameters)
        } else {
            val functionMethod = functionMethodsTable.getEntry(name)
            FunctionMethodCallAST(functionMethod.signature, callParameters)
        }
    }

    private fun visitParametersForCall(ctx: CallParametersContext): List<ExpressionAST> =
        ctx.expression().map { expr -> visitExpression(expr) }

    override fun visitIdentifier(ctx: IdentifierContext): IdentifierAST {
        val name = visitIdentifierName(ctx)
        return findIdentifier(name) // return actual identifier or one with dummy type
    }

    private fun findIdentifier(name: String): IdentifierAST = if (identifiersTable.hasEntry(name)) {
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
        IntegerLiteralAST(ctx.INT_LITERAL().toString())

    override fun visitCharLiteral(ctx: CharLiteralContext): CharacterLiteralAST =
        if (ctx.CHAR_CHAR() != null) {
            CharacterLiteralAST(ctx.CHAR_CHAR().toString()[0])
        } else {
            CharacterLiteralAST(ctx.ESCAPED_CHAR().toString()[0])
        }

    override fun visitRealLiteral(ctx: RealLiteralContext): RealLiteralAST =
        RealLiteralAST(ctx.REAL_LITERAL().toString())

    override fun visitUnaryOperator(ctx: UnaryOperatorContext): UnaryOperator = when {
        ctx.NEG() != null -> NegationOperator
        ctx.NOT() != null -> NotOperator
        else -> throw UnsupportedOperationException("Visiting unsupported unary operator $ctx")
    }

    /* ============================================== TYPE ============================================= */

    override fun visitType(ctx: TypeContext): Type = when {
        ctx.BOOL() != null -> BoolType
        ctx.INT() != null -> IntType
        ctx.CHAR() != null -> CharType
        ctx.REAL() != null -> RealType
        ctx.arrayType() != null -> visitArrayType(ctx.arrayType())
        else -> throw UnsupportedOperationException("Visiting unrecognised type context $ctx")
    }

    override fun visitArrayType(ctx: ArrayTypeContext): ArrayType {
        val innerType = visitType(ctx.genericInstantiation().type(0))
        return ArrayType(innerType)
    }
}
