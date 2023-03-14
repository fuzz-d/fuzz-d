package fuzzd.recondition.visitor

import dafnyParser.ProgramContext
import fuzzd.generator.ast.ClassTemplateAST
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.FunctionMethodSignatureAST
import fuzzd.generator.ast.MethodSignatureAST
import fuzzd.generator.ast.TraitAST

class DafnyVisitorHandler {
    fun visitProgram(ctx: ProgramContext): DafnyAST {
        val classTemplatesTable = VisitorSymbolTable<ClassTemplateAST>()
        val classFieldsTable = VisitorSymbolTable<IdentifierAST>()
        val functionMethodsTable = VisitorSymbolTable<FunctionMethodSignatureAST>()
        val methodsTable = VisitorSymbolTable<MethodSignatureAST>()
        val traitsTable = VisitorSymbolTable<TraitAST>()

        TopLevelVisitor(
            classTemplatesTable,
            traitsTable,
            functionMethodsTable,
            methodsTable,
            classFieldsTable,
        ).visitProgram(ctx) // form top level view of program

        return FullVisitor(
            classTemplatesTable,
            classFieldsTable,
            traitsTable,
            functionMethodsTable,
            methodsTable,
        ).visitProgram(ctx)
    }
}
