package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.TraitAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunctionSymbolTableTests {

    private val functionSymbolTable = FunctionSymbolTable()
    private val childFunctionSymbolTable = FunctionSymbolTable(functionSymbolTable)

    @Test
    fun givenFSTWithoutParent_whenTopLevel_expectInstanceReturned() {
        // when
        val topLevel = functionSymbolTable.topLevel()

        // expect
        assertEquals(functionSymbolTable, topLevel)
    }

    @Test
    fun givenFSTWithParent_whenTopLevel_expectParentReturned() {
        // when
        val topLevel = childFunctionSymbolTable.topLevel()

        // expect
        assertEquals(functionSymbolTable, topLevel)
    }

    @Test
    fun givenFST_whenAddFunctionMethod_expectFSTStoresFunctionMethod() {
        // when
        functionSymbolTable.addFunctionMethod(FUNCTION_METHOD_INT)

        // expect
        assertEquals(setOf(FUNCTION_METHOD_INT), functionSymbolTable.functionMethods())
    }

    @Test
    fun givenFSTWithIntTypeFunctionMethod_whenWithFunctionMethodTypeInt_expectFunctionMethodReturned() {
        // given
        functionSymbolTable.addFunctionMethod(FUNCTION_METHOD_INT)

        // when
        val withType = functionSymbolTable.withFunctionMethodType(IntType)

        // expect
        assertEquals(listOf(FUNCTION_METHOD_INT), withType)
    }

    @Test
    fun givenFSTWithIntTypeFunction_whenWithFunctionMethodTypeBool_expectEmptyList() {
        // given
        functionSymbolTable.addFunctionMethod(FUNCTION_METHOD_INT)

        // when
        val withType = functionSymbolTable.withFunctionMethodType(BoolType)

        // expect
        assertEquals(emptyList(), withType)
    }

    @Test
    fun givenFSTWithParent_whenWithFunctionMethodType_expectResultsFromParentAlso() {
        // given
        functionSymbolTable.addFunctionMethod(FUNCTION_METHOD_INT)

        // when
        val withType = childFunctionSymbolTable.withFunctionMethodType(IntType)

        // expect
        assertEquals(listOf(FUNCTION_METHOD_INT), withType)
    }

    @Test
    fun givenFST_whenAddFunctionMethods_expectAllFunctionMethodsStored() {
        // given
        functionSymbolTable.addFunctionMethods(listOf(FUNCTION_METHOD_INT, FUNCTION_METHOD_BOOL))

        // when
        val functionMethods = functionSymbolTable.functionMethods()

        // expect
        assertEquals(setOf(FUNCTION_METHOD_INT, FUNCTION_METHOD_BOOL), functionMethods)
    }

    @Test
    fun givenFSTWithFunctionMethodsInParent_whenFunctionMethods_expectParentFunctionMethodsReturned() {
        // given
        functionSymbolTable.addFunctionMethod(FUNCTION_METHOD_INT)

        // when
        val functionMethods = childFunctionSymbolTable.functionMethods()

        // expect
        assertEquals(setOf(FUNCTION_METHOD_INT), functionMethods)
    }

    @Test
    fun givenFST_whenAddMethod_expectMethodStored() {
        // given
        functionSymbolTable.addMethod(METHOD_INT)

        // when
        val methods = functionSymbolTable.methods()

        // expect
        assertEquals(setOf(METHOD_INT), methods)
    }

    @Test
    fun givenFSTWithMethodsInParent_whenMethods_expectParentMethodsReturned() {
        // given
        functionSymbolTable.addMethod(METHOD_INT)

        // when
        val methods = childFunctionSymbolTable.methods()

        // expect
        assertEquals(setOf(METHOD_INT), methods)
    }

    @Test
    fun givenFST_whenAddMethods_expectMethodsStored() {
        // given
        functionSymbolTable.addMethods(listOf(METHOD_INT, METHOD_BOOL))

        // when
        val methods = functionSymbolTable.methods()

        // expect
        assertEquals(setOf(METHOD_INT, METHOD_BOOL), methods)
    }

    @Test
    fun givenFSTWithMethods_whenAddMethodsEmptyList_expectStoredMethodsUnchanged() {
        // given
        functionSymbolTable.addMethod(METHOD_INT)

        // when
        functionSymbolTable.addMethods(listOf())

        // expect
        assertEquals(setOf(METHOD_INT), functionSymbolTable.methods())
    }

    @Test
    fun givenFSTWithNoParent_whenAddTrait_expectTraitStoredInCurrentFST() {
        // when
        functionSymbolTable.addTrait(TRAIT_T1)

        // expect
        val traits = functionSymbolTable.traits()
        assertEquals(setOf(TRAIT_T1), traits)
    }

    @Test
    fun givenFSTWithParent_whenAddTrait_expectTraitStoredInParentFST() {
        // when
        childFunctionSymbolTable.addTrait(TRAIT_T1)

        // expect
        val traits = functionSymbolTable.traits()
        assertEquals(setOf(TRAIT_T1), traits)
    }

    @Test
    fun givenFST_whenAddTrait_expectTraitStored() {
        // when
        functionSymbolTable.addTrait(TRAIT_T1)

        // expect
        val traits = functionSymbolTable.traits()
        assertEquals(setOf(TRAIT_T1), traits)
    }

    @Test
    fun givenFSTWithNoParent_whenAddClass_expectClassStoredInCurrentFST() {
        // when
        functionSymbolTable.addClass(CLASS_C1)

        // expect
        val classes = functionSymbolTable.classes()
        assertEquals(setOf(CLASS_C1), classes)
    }

    @Test
    fun givenFSTWithParent_whenAddClass_expectClassStoredInParentFST() {
        // when
        childFunctionSymbolTable.addClass(CLASS_C1)

        // expect
        val classes = functionSymbolTable.classes()
        assertEquals(setOf(CLASS_C1), classes)
    }

    @Test
    fun givenFST_whenAddClass_expectClassStored() {
        // when
        functionSymbolTable.addClass(CLASS_C1)

        // expect
        assertTrue { functionSymbolTable.hasClasses() }

        val classes = functionSymbolTable.classes()
        assertEquals(setOf(CLASS_C1), classes)
    }

    @Test
    fun givenFSTWithClassesInParent_whenHasClasses_expectTrue() {
        // when
        functionSymbolTable.addClass(CLASS_C1)

        // expect
        assertTrue { childFunctionSymbolTable.hasClasses() }
    }

    @Test
    fun givenFST_whenAddClasses_expectClassesStored() {
        // when
        functionSymbolTable.addClasses(listOf(CLASS_C1, CLASS_C2))

        // expect
        assertTrue { functionSymbolTable.hasClasses() }

        val classes = functionSymbolTable.classes()
        assertEquals(setOf(CLASS_C1, CLASS_C2), classes)
    }

    companion object {
        private val CLASS_C1 = ClassAST.builder().withName("C1").build()
        private val CLASS_C2 = ClassAST.builder().withName("C2").build()
        private val TRAIT_T1 = TraitAST.builder().withName("T1").build()
        private val TRAIT_T2 = TraitAST.builder().withName("T2").build()

        private val METHOD_INT = MethodAST(
            "m1",
            listOf(),
            listOf(IdentifierAST("r1", IntType)),
            SequenceAST(listOf(AssignmentAST(IdentifierAST("r1", IntType), IntegerLiteralAST(42)))),
        )

        private val METHOD_BOOL = MethodAST(
            "m2",
            listOf(),
            listOf(IdentifierAST("r1", BoolType)),
            SequenceAST(listOf(AssignmentAST(IdentifierAST("r1", BoolType), BooleanLiteralAST(true)))),
        )

        private val FUNCTION_METHOD_INT = FunctionMethodAST("fm1", IntType, listOf(), IntegerLiteralAST(42))
        private val FUNCTION_METHOD_BOOL = FunctionMethodAST("fm2", BoolType, listOf(), BooleanLiteralAST(false))
    }
}
