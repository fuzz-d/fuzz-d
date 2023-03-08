package fuzzd.recondition

import dafnyBaseVisitor
import dafnyParser.ClassDeclContext
import dafnyParser.FunctionDeclContext
import dafnyParser.MethodDeclContext
import dafnyParser.ProgramContext
import dafnyParser.TopDeclContext
import dafnyParser.TopDeclMemberContext
import dafnyParser.TraitDeclContext
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST

class DafnyVisitor : dafnyBaseVisitor<ASTElement>() {
    override fun visitProgram(ctx: ProgramContext): DafnyAST =
        DafnyAST(
            (ctx.topDecl()?.map { topDeclCtx -> visitTopDecl(topDeclCtx) }) ?: listOf()
        )

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = visit(ctx) as TopLevelAST

    override fun visitClassDecl(ctx: ClassDeclContext): ClassAST {
        TODO()
    }

    override fun visitTraitDecl(ctx: TraitDeclContext): TraitAST {
        TODO()
    }

    override fun visitTopDeclMember(ctx: TopDeclMemberContext): TopLevelAST = visit(ctx) as TopLevelAST

    override fun visitFunctionDecl(ctx: FunctionDeclContext): FunctionMethodAST {
        TODO()
    }

    override fun visitMethodDecl(ctx: MethodDeclContext?): ASTElement {
        TODO()
    }
}
