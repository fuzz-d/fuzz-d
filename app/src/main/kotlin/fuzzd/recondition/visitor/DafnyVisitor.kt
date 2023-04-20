package fuzzd.recondition.visitor

import dafnyBaseVisitor
import dafnyParser.* // ktlint-disable no-wildcard-imports
import fuzzd.generator.Generator.Companion.GLOBAL_STATE
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
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
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceFieldAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IndexAST
import fuzzd.generator.ast.ExpressionAST.IndexAssignAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.LiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
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
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.StatementAST.BreakAST
import fuzzd.generator.ast.StatementAST.IfStatementAST
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
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.LiteralType
import fuzzd.generator.ast.Type.MapType
import fuzzd.generator.ast.Type.MethodReturnType
import fuzzd.generator.ast.Type.MultisetType
import fuzzd.generator.ast.Type.PlaceholderType
import fuzzd.generator.ast.Type.RealType
import fuzzd.generator.ast.Type.SequenceType
import fuzzd.generator.ast.Type.SetType
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
import fuzzd.utils.SAFE_ADDITION_INT
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.SAFE_MULTIPLY_INT
import fuzzd.utils.SAFE_SUBTRACT_INT
import fuzzd.utils.toHexInt
import fuzzd.utils.unionAll

class DafnyVisitor : dafnyBaseVisitor<ASTElement>() {
    private val classesTable = VisitorSymbolTable<ClassAST>()
    private val classFieldsTable = VisitorSymbolTable<IdentifierAST>()
    private val traitsTable = VisitorSymbolTable<TraitAST>()
    private var functionMethodsTable = VisitorSymbolTable<FunctionMethodSignatureAST>()
    private var methodsTable = VisitorSymbolTable<MethodSignatureAST>()
    private var identifiersTable = VisitorSymbolTable<IdentifierAST>()

    /* ============================================ TOP LEVEL ============================================ */
    override fun visitProgram(ctx: ProgramContext): DafnyAST {
        listOf(
            SAFE_ADDITION_INT,
            SAFE_DIVISION_INT,
            SAFE_MODULO_INT,
            SAFE_MULTIPLY_INT,
            SAFE_SUBTRACT_INT,
            ABSOLUTE,
        ).forEach { functionMethodsTable.addEntry(it.name(), it.signature) }

        val globalStateCtx = ctx.topDecl().first {
            it.classDecl() != null && visitIdentifierName(it.classDecl().identifier(0)) == GLOBAL_STATE
        }

        visitClassDecl(globalStateCtx.classDecl())

        val topLevelContexts = ctx.topDecl().filter { it.topDeclMember() != null }.map { it.topDeclMember() }

        topLevelContexts.forEach { visitTopDeclMemberPrimaryPass(it) }

        val traits = ctx.topDecl().filter { it.traitDecl() != null }.map { visitTraitDecl(it.traitDecl()) }
        val classes = ctx.topDecl().filter { it.classDecl() != null }.map { visitClassDecl(it.classDecl()) }
        val topLevelMembers = topLevelContexts.map { visitTopDeclMember(it) }

        return DafnyAST(topLevelMembers + traits + classes)
    }

    private fun visitTopDeclMemberPrimaryPass(ctx: TopDeclMemberContext) {
        if (ctx.functionDecl() != null) {
            visitFunctionSignatureDecl(ctx.functionDecl().functionSignatureDecl())
        } else {
            visitMethodSignatureDecl(ctx.methodDecl().methodSignatureDecl())
        }
    }

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = super.visitTopDecl(ctx) as TopLevelAST

    override fun visitClassDecl(ctx: ClassDeclContext): ClassAST {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx -> visitIdentifierName(identifierCtx) }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }.toSet()

        functionMethodsTable = functionMethodsTable.increaseDepth()
        methodsTable = methodsTable.increaseDepth()
        identifiersTable = identifiersTable.increaseDepth()

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
            .map { identifierCtx -> visitIdentifierName(identifierCtx) }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }.toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        functionMethodsTable = functionMethodsTable.increaseDepth()
        methodsTable = methodsTable.increaseDepth()
        identifiersTable = identifiersTable.increaseDepth()

        ctx.traitMemberDecl().forEach { traitMemberDeclCtx ->
            when (val astNode = super.visitTraitMemberDecl(traitMemberDeclCtx)) {
                is IdentifierAST -> {
                    fields.add(astNode); classFieldsTable.addEntry(astNode.name, astNode)
                }

                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        functionMethodsTable = functionMethodsTable.decreaseDepth()
        methodsTable = methodsTable.decreaseDepth()
        identifiersTable = identifiersTable.decreaseDepth()

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
        val name = visitIdentifierName(ctx.identifier())

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
        val name = visitIdentifierName(ctx.identifier())

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
            IdentifierAST(
                identifier.name,
                identifier.type(),
                mutable = false,
                initialised = true,
            )
        }

    override fun visitSequence(ctx: SequenceContext): SequenceAST =
        SequenceAST(ctx.statement().map(this::visitStatement))

    override fun visitIdentifierType(ctx: IdentifierTypeContext): IdentifierAST =
        IdentifierAST(visitIdentifierName(ctx.identifier()), visitType(ctx.type()))

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
            val identifier = if (type is ClassType) {
                val identifier = ClassInstanceAST(type.clazz, lhs[i].name)

                type.clazz.fields.map { ident -> ClassInstanceFieldAST(identifier, ident) }
                    .forEach { identifiersTable.addEntry(it.name, it) }

                type.clazz.functionMethods.map { fm ->
                    ClassInstanceFunctionMethodSignatureAST(
                        identifier,
                        fm.signature,
                    )
                }.forEach { functionMethodsTable.addEntry(it.name, it) }

                type.clazz.methods.map { m -> ClassInstanceMethodSignatureAST(identifier, m.signature) }
                    .forEach { methodsTable.addEntry(it.name, it) }

                identifier
            } else {
                IdentifierAST(lhs[i].name, rhsTypes[i], mutable = true, initialised = true)
            }

            identifiersTable.addEntry(identifier.name, identifier)

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
            when (identifier.type()) {
                is ArrayType -> ArrayIndexAST(identifier, index)
                is SequenceType -> SequenceIndexAST(identifier, index)
                else -> IndexAST(identifier, index)
            }
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

    override fun visitExpression(ctx: ExpressionContext): ExpressionAST = when {
        ctx.literal() != null -> visitLiteral(ctx.literal())
        ctx.functionCall() != null -> visitFunctionCall(ctx.functionCall())
        ctx.declAssignLhs() != null -> visitDeclAssignLhs(ctx.declAssignLhs())
        ctx.unaryOperator() != null -> UnaryExpressionAST(
            visitExpression(ctx.expression(0)),
            visitUnaryOperator(ctx.unaryOperator()),
        )

        ctx.modulus() != null -> visitModulus(ctx.modulus())
        ctx.multisetConversion() != null -> visitMultisetConversion(ctx.multisetConversion())

        ctx.classInstantiation() != null -> visitClassInstantiation(ctx.classInstantiation())
        ctx.ternaryExpression() != null -> visitTernaryExpression(ctx.ternaryExpression())
        ctx.arrayLength() != null -> visitArrayLength(ctx.arrayLength())
        ctx.setDisplay() != null -> visitSetDisplay(ctx.setDisplay())
        ctx.sequenceDisplay() != null -> visitSequenceDisplay(ctx.sequenceDisplay())
        ctx.mapConstructor() != null -> visitMapConstructor(ctx.mapConstructor())
        ctx.indexAssign() != null -> visitIndexAssign(ctx.indexAssign())

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
        val className = visitIdentifierName(ctx.identifier())
        val callParams = visitParametersForCall(ctx.callParameters())

        if (!classesTable.hasEntry(className)) {
            throw UnsupportedOperationException("Visiting instantiation for unknown class $className")
        }

        return ClassInstantiationAST(classesTable.getEntry(className), callParams)
    }

    override fun visitTernaryExpression(ctx: TernaryExpressionContext): TernaryExpressionAST {
        val visitedExprs = ctx.expression().map { expr -> visitExpression(expr) }
        return TernaryExpressionAST(visitedExprs[0], visitedExprs[1], visitedExprs[2])
    }

    override fun visitArrayConstructor(ctx: ArrayConstructorContext): ArrayInitAST {
        val innerType = visitType(ctx.type())
        val dimension = visitIntLiteral(ctx.intLiteral(0)).toString().toInt()

        return ArrayInitAST(dimension, ArrayType(innerType))
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

    override fun visitSequenceDisplay(ctx: SequenceDisplayContext): SequenceDisplayAST {
        val exprs = ctx.expression().map(this::visitExpression)
        return SequenceDisplayAST(exprs)
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

    override fun visitIndexAssign(ctx: IndexAssignContext): IndexAssignAST {
        val ident = visitDeclAssignLhs(ctx.declAssignLhs())
        val assign = visitIndexElement(ctx.indexElem())

        return IndexAssignAST(ident, assign.first, assign.second)
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
        IntegerLiteralAST(ctx.INT_LITERAL().toString().toHexInt())

    override fun visitCharLiteral(ctx: CharLiteralContext): CharacterLiteralAST = if (ctx.CHAR_CHAR() != null) {
        CharacterLiteralAST(ctx.CHAR_CHAR().toString()[0])
    } else {
        CharacterLiteralAST(ctx.ESCAPED_CHAR().toString()[0])
    }

    override fun visitStringToken(ctx: StringTokenContext): StringLiteralAST =
        StringLiteralAST(ctx.text.substring(1, ctx.text.length - 1))

    override fun visitRealLiteral(ctx: RealLiteralContext): RealLiteralAST =
        RealLiteralAST(ctx.REAL_LITERAL().toString())

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
        ctx.REAL() != null -> RealType
        ctx.arrayType() != null -> visitArrayType(ctx.arrayType())
        ctx.identifier() != null -> visitIdentifierType(ctx.identifier())
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

    private fun visitIdentifierType(ctx: IdentifierContext): ClassType {
        val name = visitIdentifierName(ctx)
        return ClassType(classesTable.getEntry(name))
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
}
