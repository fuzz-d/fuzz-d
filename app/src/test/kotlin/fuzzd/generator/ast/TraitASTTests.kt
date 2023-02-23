package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TraitASTTests {

    @Test
    fun givenTraitWithExtends_whenExtends_expectAllTraits() {
        // given
        val trait1 = TraitAST.builder().withName("${TRAIT_NAME}1").build()
        val trait2 = TraitAST.builder().withName("${TRAIT_NAME}2").withExtends(setOf(trait1)).build()
        val trait3 = TraitAST.builder().withName("${TRAIT_NAME}3").withExtends(setOf(trait2)).build()
        val trait4 = TraitAST.builder().withName("${TRAIT_NAME}4").build()
        val trait5 = TraitAST.builder().withName("${TRAIT_NAME}5").withExtends(setOf(trait4)).build()

        val trait = TraitAST.builder().withName(TRAIT_NAME).withExtends(setOf(trait3, trait5)).build()

        // when
        val extends = trait.extends()

        // expect
        assertEquals(extends, setOf(trait1, trait2, trait3, trait4, trait5))
    }

    @Test
    fun givenTraitWithExtends_whenFields_expectAllTraitFields() {
        // given
        val trait1 =
            TraitAST.builder().withName("${TRAIT_NAME}1").withFields(setOf(FIELD_1)).build()
        val trait2 =
            TraitAST.builder().withName("${TRAIT_NAME}2").withFields(setOf(FIELD_2)).build()

        val trait = TraitAST.builder().withName(TRAIT_NAME).withExtends(setOf(trait1, trait2)).build()

        // when
        val fields = trait.fields()

        // expect
        assertEquals(FIELDS, fields)
    }

    @Test
    fun givenTraitWithExtends_whenFunctionMethods_expectAllFunctionMethods() {
        // given
        val trait1 = TraitAST.builder().withName("${TRAIT_NAME}1").withFunctionMethods(setOf(FUNCTION_METHOD_1)).build()
        val trait2 = TraitAST.builder().withName("${TRAIT_NAME}2").withFunctionMethods(setOf(FUNCTION_METHOD_2)).build()

        val trait = TraitAST.builder().withName(TRAIT_NAME).withExtends(setOf(trait1, trait2)).build()

        // when
        val functionMethods = trait.functionMethods()

        // expect
        assertEquals(FUNCTION_METHODS, functionMethods)
    }

    @Test
    fun givenTraitWithExtends_whenMethods_expectAllMethods() {
        // given
        val trait1 = TraitAST.builder().withName("${TRAIT_NAME}1").withMethods(setOf(METHOD_1)).build()
        val trait2 = TraitAST.builder().withName("${TRAIT_NAME}2").withMethods(setOf(METHOD_2)).build()

        val trait = TraitAST.builder().withName(TRAIT_NAME).withExtends(setOf(trait1, trait2)).build()

        // when
        val methods = trait.methods()

        // expect
        assertEquals(METHODS, methods)
    }

    @Test
    fun givenTraitWithNoExtends_whenToString_expectNoExtends() {
        // given
        val trait = TraitAST(TRAIT_NAME, setOf(), FUNCTION_METHODS, METHODS, FIELDS)

        // when
        val str = trait.toString()

        // expect
        print(str)
        assertFalse(str.contains("extends"))
    }

    companion object {
        private const val TRAIT_NAME = "TestTrait"

        // default function method instances
        private val FUNCTION_METHOD_1 = FunctionMethodSignatureAST("fm1", IntType, listOf(IdentifierAST("p1", IntType)))
        private val FUNCTION_METHOD_2 =
            FunctionMethodSignatureAST("fm2", BoolType, listOf(IdentifierAST("p1", BoolType)))
        val FUNCTION_METHODS = setOf(FUNCTION_METHOD_1, FUNCTION_METHOD_2)

        // default method instances
        private val METHOD_1 =
            MethodSignatureAST("m1", listOf(IdentifierAST("p1", IntType)), listOf(IdentifierAST("r1", IntType)))
        private val METHOD_2 =
            MethodSignatureAST("m2", listOf(IdentifierAST("p1", BoolType)), listOf(IdentifierAST("r1", BoolType)))
        private val METHODS = setOf(METHOD_1, METHOD_2)

        // default field instances
        private val FIELD_1 = IdentifierAST("x", IntType)
        private val FIELD_2 = IdentifierAST("y", BoolType)
        private val FIELDS = setOf(FIELD_1, FIELD_2)
    }
}
