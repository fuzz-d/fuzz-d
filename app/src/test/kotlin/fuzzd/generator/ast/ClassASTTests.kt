package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.StatementAST.PrintAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.expectFailure
import org.junit.jupiter.api.Test

class ClassASTTests {

    @Test
    fun givenClassASTWithExtends_whenInitWithMissingFields_expectInvalidInputException() {
        expectFailure<InvalidInputException>("Missing fields for class declaration C1: f2") {
            ClassAST.builder().withName("C1").withExtends(TRAITS).withInheritedFields(setOf(FIELD_1)).build()
        }
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithMissingFunctionMethods_expectInvalidInputException() {
        // given
        val functionMethod = FunctionMethodAST(FUNCTION_METHOD_SIG_1, FUNCTION_METHOD_BODY)

        // expect
        expectFailure<InvalidInputException>("Missing function methods for class declaration C1: fm2") {
            ClassAST.builder().withName("C1").withExtends(TRAITS).withInheritedFields(FIELDS)
                .withFunctionMethods(setOf(functionMethod)).build()
        }
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithMissingMethods_expectInvalidInputException() {
        // given
        val functionMethods = FUNCTION_METHOD_SIGS.map { sig -> FunctionMethodAST(sig, FUNCTION_METHOD_BODY) }.toSet()
        val method = MethodAST(METHOD_SIG_1)
        method.setBody(METHOD_BODY)

        // expect
        expectFailure<InvalidInputException>("Missing methods for class declaration C1: m2") {
            ClassAST.builder().withName("C1").withExtends(TRAITS).withInheritedFields(FIELDS)
                .withFunctionMethods(functionMethods).withMethods(setOf(method)).build()
        }
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithTraitValues_expectSuccessfulInit() {
        // given
        val functionMethods = FUNCTION_METHOD_SIGS.map { sig -> FunctionMethodAST(sig, FUNCTION_METHOD_BODY) }.toSet()
        val methods = METHOD_SIGS.map { sig -> val m = MethodAST(sig); m.setBody(METHOD_BODY); m }.toSet()

        // expect nothing
        ClassAST.builder().withName("C1").withExtends(TRAITS).withInheritedFields(FIELDS).withFunctionMethods(functionMethods)
            .withMethods(methods).toString()
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithAdditionalFields_expectSuccessfulInit() {
        // given
        val functionMethod = FunctionMethodAST(FUNCTION_METHOD_SIG_1, FUNCTION_METHOD_BODY)
        val method = MethodAST(METHOD_SIG_1)
        method.setBody(METHOD_BODY)

        // expect nothing
        ClassAST.builder().withName("C1")
            .withExtends(setOf(TRAIT_1))
            .withFields(setOf(FIELD_2))
            .withInheritedFields(setOf(FIELD_1))
            .withFunctionMethods(setOf(functionMethod))
            .withMethods(setOf(method))
            .build()
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithAdditionalInheritedFields_expectInvalidInputException() {
        // given
        val functionMethod = FunctionMethodAST(FUNCTION_METHOD_SIG_1, FUNCTION_METHOD_BODY)
        val method = MethodAST(METHOD_SIG_1)
        method.setBody(METHOD_BODY)

        // expect nothing
        // TODO: Should fail since f2 not inherited.
        expectFailure<InvalidInputException>("Too many trait fields for class declaration C1: f2") {
            ClassAST.builder().withName("C1").withExtends(setOf(TRAIT_1)).withInheritedFields(FIELDS)
                .withFunctionMethods(setOf(functionMethod)).withMethods(setOf(method)).build()
        }
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithAdditionalFunctionMethods_expectSuccessfulInit() {
        // given
        val functionMethods = FUNCTION_METHOD_SIGS.map { sig -> FunctionMethodAST(sig, FUNCTION_METHOD_BODY) }.toSet()
        val method = MethodAST(METHOD_SIG_1)
        method.setBody(METHOD_BODY)

        // expect nothing
        ClassAST.builder().withName("C1").withExtends(setOf(TRAIT_1)).withInheritedFields(setOf(FIELD_1))
            .withFunctionMethods(functionMethods).withMethods(setOf(method)).build()
    }

    @Test
    fun givenClassASTWithExtends_whenInitWithAdditionalMethods_expectSuccessfulInit() {
        // given
        val functionMethod = FunctionMethodAST(FUNCTION_METHOD_SIG_1, FUNCTION_METHOD_BODY)
        val methods = METHOD_SIGS.map { sig -> val m = MethodAST(sig); m.setBody(METHOD_BODY); m }.toSet()

        // expect nothing
        ClassAST.builder().withName("C1").withExtends(setOf(TRAIT_1)).withInheritedFields(setOf(FIELD_1))
            .withFunctionMethods(setOf(functionMethod)).withMethods(methods).build()
    }

    @Test
    fun givenClassASTWithNoExtends_whenInit_expectSuccessfulInit() {
        // given
        val functionMethods = FUNCTION_METHOD_SIGS.map { sig -> FunctionMethodAST(sig, FUNCTION_METHOD_BODY) }.toSet()
        val methods = METHOD_SIGS.map { sig -> val m = MethodAST(sig); m.setBody(METHOD_BODY); m }.toSet()

        // expect nothing
        ClassAST.builder().withName("C1").withFields(FIELDS).withFunctionMethods(functionMethods).withMethods(methods)
            .build()
    }

    companion object {
        private val METHOD_BODY = SequenceAST(listOf(PrintAST(IntegerLiteralAST(42))))
        private val FUNCTION_METHOD_BODY = IntegerLiteralAST(42)

        private val FUNCTION_METHOD_SIG_1 = createFunctionMethodSignature("fm1")
        private val FUNCTION_METHOD_SIG_2 = createFunctionMethodSignature("fm2")
        private val FUNCTION_METHOD_SIGS = setOf(FUNCTION_METHOD_SIG_1, FUNCTION_METHOD_SIG_2)

        private val METHOD_SIG_1 = createMethodSignature("m1")
        private val METHOD_SIG_2 = createMethodSignature("m2")
        private val METHOD_SIGS = setOf(METHOD_SIG_1, METHOD_SIG_2)

        private val FIELD_1 = IdentifierAST("f1", IntType)
        private val FIELD_2 = IdentifierAST("f2", BoolType)
        private val FIELDS = setOf(FIELD_1, FIELD_2)

        private val TRAIT_1 = TraitAST.builder().withName("T1").withFields(setOf(FIELD_1))
            .withFunctionMethods(setOf(FUNCTION_METHOD_SIG_1)).withMethods(setOf(METHOD_SIG_1)).build()

        private val TRAIT_2 = TraitAST.builder().withName("T2").withFields(setOf(FIELD_2))
            .withFunctionMethods(setOf(FUNCTION_METHOD_SIG_2)).withMethods(setOf(METHOD_SIG_2)).build()

        private val TRAITS = setOf(TRAIT_1, TRAIT_2)

        private fun createMethodSignature(name: String) = MethodSignatureAST(name, listOf(), listOf())
        private fun createFunctionMethodSignature(name: String) = FunctionMethodSignatureAST(name, IntType, listOf())
    }
}
