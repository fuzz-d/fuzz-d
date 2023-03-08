package fuzzd.recondition

import dafnyBaseVisitor
import dafnyParser.AssignmentContext
import dafnyParser.BreakStatementContext
import dafnyParser.ClassDeclContext
import dafnyParser.ClassMemberDeclContext
import dafnyParser.ContinueStatementContext
import dafnyParser.DeclarationContext
import dafnyParser.ExpressionContext
import dafnyParser.FieldDeclContext
import dafnyParser.FunctionDeclContext
import dafnyParser.FunctionSignatureDeclContext
import dafnyParser.IdentifierContext
import dafnyParser.IdentifierTypeContext
import dafnyParser.IfStatementContext
import dafnyParser.MethodDeclContext
import dafnyParser.MethodSignatureDeclContext
import dafnyParser.ParametersContext
import dafnyParser.PrintContext
import dafnyParser.ProgramContext
import dafnyParser.StatementContext
import dafnyParser.TopDeclContext
import dafnyParser.TopDeclMemberContext
import dafnyParser.TraitDeclContext
import dafnyParser.TypeContext
import dafnyParser.WhileStatementContext
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
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
import fuzzd.utils.unionAll

class DafnyVisitor : dafnyBaseVisitor<ASTElement>() {
    private val traits = mutableMapOf<String, TraitAST>() // get trait object by name

    override fun visitProgram(ctx: ProgramContext): DafnyAST =
        DafnyAST(
            (ctx.topDecl()?.map { topDeclCtx -> visitTopDecl(topDeclCtx) }) ?: listOf()
        )

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = super.visitTopDecl(ctx) as TopLevelAST

    override fun visitClassDecl(ctx: ClassDeclContext): ClassAST {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx ->
                identifierCtx.IDENTIFIER().toString()
            }
            .map { identifierStr -> traits[identifierStr]!! }
            .toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodAST>()
        val methods = mutableSetOf<MethodAST>()

        ctx.classMemberDecl().forEach { classMemberDeclCtx ->
            when (val astNode = super.visitClassMemberDecl(classMemberDeclCtx)) {
                is IdentifierAST -> fields.add(astNode)
                is FunctionMethodAST -> functionMethods.add(astNode)
                is MethodAST -> methods.add(astNode)
                else -> {} // do nothing
            }
        }

        val inheritedFields = extends.map { it.fields() }.unionAll()

        return ClassAST(name, extends, functionMethods, methods, fields, inheritedFields)
    }

    override fun visitTraitDecl(ctx: TraitDeclContext): TraitAST {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx ->
                identifierCtx.IDENTIFIER().toString()
            }
            .map { identifierStr -> traits[identifierStr]!! }
            .toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        ctx.traitMemberDecl().forEach { traitMemberDeclCtx ->
            when (val astNode = super.visitTraitMemberDecl(traitMemberDeclCtx)) {
                is IdentifierAST -> fields.add(astNode)
                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        val trait = TraitAST(name, extends, functionMethods, methods, fields)

        traits[name] = trait

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
        val body = visitExpression(ctx.expression())

        return FunctionMethodAST(signature, body)
    }

    override fun visitMethodDecl(ctx: MethodDeclContext): ASTElement {
        val signature = visitMethodSignatureDecl(ctx.methodSignatureDecl())
        val body = SequenceAST(ctx.statement().map(this::visitStatement))

        val method = MethodAST(signature)
        method.setBody(body)

        return method
    }

    override fun visitMethodSignatureDecl(ctx: MethodSignatureDeclContext): MethodSignatureAST {
        val name = visitIdentifierName(ctx.identifier())

        val params = visitParametersList(ctx.parameters(0))
        val returns = if (ctx.parameters().size > 1) visitParametersList(ctx.parameters(1)) else emptyList()

        return MethodSignatureAST(name, params, returns)
    }

    private fun visitParametersList(ctx: ParametersContext): List<IdentifierAST> =
        ctx.identifierType().map { identifierTypeCtx -> visitIdentifierType(identifierTypeCtx) }

    override fun visitIdentifierType(ctx: IdentifierTypeContext): IdentifierAST =
        IdentifierAST(visitIdentifierName(ctx.identifier()), visitType(ctx.type()))

    override fun visitStatement(ctx: StatementContext): StatementAST = super.visitStatement(ctx) as StatementAST

    override fun visitBreakStatement(ctx: BreakStatementContext): BreakAST = BreakAST

    override fun visitDeclaration(ctx: DeclarationContext): DeclarationAST = TODO()

    override fun visitAssignment(ctx: AssignmentContext): AssignmentAST = TODO()

    override fun visitPrint(ctx: PrintContext): PrintAST = TODO()

    override fun visitIfStatement(ctx: IfStatementContext): IfStatementAST = TODO()

    override fun visitWhileStatement(ctx: WhileStatementContext): WhileLoopAST = TODO()

    override fun visitExpression(ctx: ExpressionContext): ExpressionAST = super.visitExpression(ctx) as ExpressionAST

    override fun visitType(ctx: TypeContext): Type = TODO()

    private fun visitIdentifierName(identifierCtx: IdentifierContext): String = identifierCtx.IDENTIFIER().toString()
}
