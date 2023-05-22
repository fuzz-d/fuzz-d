package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.FunctionMethodAST
import fuzzd.generator.ast.MethodAST
import fuzzd.generator.ast.SequenceAST
import fuzzd.generator.ast.StatementAST.AssignmentAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.IntType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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

    companion object {
        private val METHOD_INT = MethodAST(
            "m1",
            listOf(),
            listOf(IdentifierAST("r1", IntType)),
            mutableListOf(),
            SequenceAST(listOf(AssignmentAST(IdentifierAST("r1", IntType), IntegerLiteralAST(42)))),
        )

        private val METHOD_BOOL = MethodAST(
            "m2",
            listOf(),
            listOf(IdentifierAST("r1", BoolType)),
            mutableListOf(),
            SequenceAST(listOf(AssignmentAST(IdentifierAST("r1", BoolType), BooleanLiteralAST(true)))),
        )

        private val FUNCTION_METHOD_INT = FunctionMethodAST("fm1", IntType, listOf(), mutableListOf(), IntegerLiteralAST(42))
        private val FUNCTION_METHOD_BOOL = FunctionMethodAST("fm2", BoolType, listOf(), mutableListOf(), BooleanLiteralAST(false))
    }
}
