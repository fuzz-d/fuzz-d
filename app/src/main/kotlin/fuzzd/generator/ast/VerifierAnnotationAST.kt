package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST

sealed class VerifierAnnotationAST : ASTElement {
    abstract override fun toString(): String

    class DecreasesAnnotation(val expr: ExpressionAST) : VerifierAnnotationAST() {
        override fun toString(): String = "decreases $expr"
    }

    class EnsuresAnnotation(val expr: ExpressionAST) : VerifierAnnotationAST() {
        override fun toString(): String = "ensures $expr"
    }

    class InvariantAnnotation(val expr: ExpressionAST) : VerifierAnnotationAST() {
        override fun toString(): String = "invariant $expr"
    }

    class ModifiesAnnotation(val identifier: IdentifierAST) : VerifierAnnotationAST() {
        override fun toString(): String = "modifies $identifier"
    }

    class ReadsAnnotation(val identifier: IdentifierAST) : VerifierAnnotationAST() {
        override fun toString(): String = "reads $identifier"
    }

    class RequiresAnnotation(val expr: ExpressionAST) : VerifierAnnotationAST() {
        override fun toString(): String = "requires $expr"
    }
}
