package fuzzd.recondition.visitor

import dafnyBaseVisitor
import dafnyParser.ArrayTypeContext
import dafnyParser.ClassDeclContext
import dafnyParser.FunctionDeclContext
import dafnyParser.FunctionSignatureDeclContext
import dafnyParser.IdentifierTypeContext
import dafnyParser.MethodDeclContext
import dafnyParser.MethodSignatureDeclContext
import dafnyParser.ParametersContext
import dafnyParser.ProgramContext
import dafnyParser.TopDeclContext
import dafnyParser.TraitDeclContext
import dafnyParser.TypeContext
import fuzzd.generator.ast.ASTElement
import fuzzd.generator.ast.ClassTemplateAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.TopLevelAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.CharType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.Type.RealType
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_ADDITION_INT
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_MODULO_INT
import fuzzd.utils.SAFE_MULTIPLY_INT
import fuzzd.utils.SAFE_SUBTRACT_INT
import fuzzd.utils.unionAll

class TopLevelVisitor(
    private val classTemplatesTable: VisitorSymbolTable<ClassTemplateAST>,
    private val traitsTable: VisitorSymbolTable<TraitAST>,
    private val functionMethodsTable: VisitorSymbolTable<FunctionMethodSignatureAST>,
    private val methodsTable: VisitorSymbolTable<MethodSignatureAST>,
    private val classFieldsTable: VisitorSymbolTable<IdentifierAST>
) : dafnyBaseVisitor<ASTElement>() {
    override fun visitProgram(ctx: ProgramContext): DafnyAST {
        // be able to recognise safety function calls
        listOf(
            SAFE_ADDITION_INT,
            SAFE_DIVISION_INT,
            SAFE_MODULO_INT,
            SAFE_MULTIPLY_INT,
            SAFE_SUBTRACT_INT,
            ABSOLUTE,
        ).forEach { functionMethodsTable.addEntry(it.name(), it.signature) }

        return DafnyAST(ctx.topDecl().map { visitTopDecl(it) })
    }

    override fun visitTopDecl(ctx: TopDeclContext): TopLevelAST = super.visitTopDecl(ctx) as TopLevelAST

    override fun visitMethodDecl(ctx: MethodDeclContext): MethodSignatureAST {
        val signature = visitMethodSignatureDecl(ctx.methodSignatureDecl())
        methodsTable.addEntry(signature.name, signature)
        return signature
    }

    override fun visitMethodSignatureDecl(ctx: MethodSignatureDeclContext): MethodSignatureAST {
        val name = visitIdentifierName(ctx.identifier())

        val params = visitParametersList(ctx.parameters(0))
        val returns = if (ctx.parameters().size > 1) visitReturnsList(ctx.parameters(1)) else emptyList()

        return MethodSignatureAST(name, params, returns)
    }

    override fun visitFunctionDecl(ctx: FunctionDeclContext): FunctionMethodSignatureAST {
        val signature = visitFunctionSignatureDecl(ctx.functionSignatureDecl())
        functionMethodsTable.addEntry(signature.name, signature)
        return signature
    }

    override fun visitFunctionSignatureDecl(ctx: FunctionSignatureDeclContext): FunctionMethodSignatureAST {
        val name = visitIdentifierName(ctx.identifier())

        val params = visitParametersList(ctx.parameters())
        val returnType = visitType(ctx.type())

        return FunctionMethodSignatureAST(name, returnType, params)
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

    override fun visitClassDecl(ctx: ClassDeclContext): ClassTemplateAST {
        val name = visitIdentifierName(ctx.identifier(0))
        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx -> visitIdentifierName(identifierCtx) }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }
            .toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        ctx.classMemberDecl().forEach { classMemberDeclCtx ->
            when (val astNode = super.visitClassMemberDecl(classMemberDeclCtx)) {
                is IdentifierAST -> { fields.add(astNode); classFieldsTable.addEntry(astNode.name, astNode) }
                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        val inheritedFields = extends.map { it.fields() }.unionAll()
        val template = ClassTemplateAST(name, extends, functionMethods, methods, fields, inheritedFields)
        classTemplatesTable.addEntry(name, template)
        return template
    }

    override fun visitTraitDecl(ctx: TraitDeclContext): ASTElement {
        val name = visitIdentifierName(ctx.identifier(0))

        val extends = ctx.identifier().slice(1 until ctx.identifier().size)
            .map { identifierCtx -> visitIdentifierName(identifierCtx) }
            .map { identifierStr -> traitsTable.getEntry(identifierStr) }
            .toSet()

        val fields = mutableSetOf<IdentifierAST>()
        val functionMethods = mutableSetOf<FunctionMethodSignatureAST>()
        val methods = mutableSetOf<MethodSignatureAST>()

        ctx.traitMemberDecl().forEach { traitMemberDeclCtx ->
            when (val astNode = super.visitTraitMemberDecl(traitMemberDeclCtx)) {
                is IdentifierAST -> {
                    fields.add(astNode); classFieldsTable.addEntry(astNode.name, astNode)
                }

                is FunctionMethodSignatureAST -> functionMethods.add(astNode)
                is MethodSignatureAST -> methods.add(astNode)
            }
        }

        val trait = TraitAST(name, extends, functionMethods, methods, fields)
        traitsTable.addEntry(name, trait)
        return trait
    }

    override fun visitIdentifierType(ctx: IdentifierTypeContext): IdentifierAST =
        IdentifierAST(visitIdentifierName(ctx.identifier()), visitType(ctx.type()))

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
