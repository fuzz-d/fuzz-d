package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.ExpressionAST.ClassInstanceAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.Type.BoolType
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class SymbolTableTests {
    private val symbolTable = SymbolTable()
    private val childSymbolTable = SymbolTable(symbolTable)

    @Test
    fun givenSymbolTableWithClassInstance_whenWithType_expectClassFieldsReturned() {
        // given
        val v1 = ClassInstanceAST(CLASS_AST, "v1")
        val v2 = IdentifierAST("v2", BoolType)

        symbolTable.add(v1)
        symbolTable.add(v2)

        // when
        val withType = symbolTable.withType(BoolType)

        // expect

        val expectedClassField = IdentifierAST("${v1.name}.${CLASS_FIELD.name}", CLASS_FIELD.type())
        assertContentEquals(listOf(v2, expectedClassField), withType)
    }

    @Test
    fun givenEmptySymbolTable_whenClassInstances_expectEmptyList() {
        // given empty

        // when
        val instances = childSymbolTable.classInstances()

        // expect
        assertTrue { instances.isEmpty() }
    }

    @Test
    fun givenSymbolTableWithClassInstance_whenClassInstances_expectClassInstanceReturned() {
        // given
        val v1 = ClassInstanceAST(CLASS_AST, "v1")

        symbolTable.add(v1)

        // when
        val instances = symbolTable.classInstances()

        // expect
        assertContentEquals(listOf(v1), instances)
    }

    @Test
    fun givenSymbolTable_whenClassInstances_expectFetchFromParentSymbol() {
        // given
        val v1 = ClassInstanceAST(CLASS_AST, "v1")
        val v2 = ClassInstanceAST(CLASS_AST, "v2")

        symbolTable.add(v1)
        childSymbolTable.add(v2)

        // when
        val instances = childSymbolTable.classInstances()

        // expect
        assertContentEquals(listOf(v1, v2), instances)
    }

    companion object {
        private val CLASS_FIELD = IdentifierAST("cf1", BoolType)
        private val CLASS_AST = ClassAST.builder()
            .withName("C1")
            .withFields(setOf(CLASS_FIELD))
            .build()
    }
}
