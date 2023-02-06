package fuzzd.generator.symbol_table

import fuzzd.generator.ast.MethodAST
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MethodCallTableTests {

    private lateinit var methodCallTable: MethodCallTable
    private val method1 = MethodAST("m1", listOf(), listOf())
    private val method2 = MethodAST("m2", listOf(), listOf())
    private val method3 = MethodAST("m3", listOf(), listOf())

    @BeforeEach
    fun setup() {
        methodCallTable = MethodCallTable()
    }

    @Test
    fun givenOneLayerDependency_whenCanCall_expectFalse() {
        methodCallTable.addCall(method2, method1) // 1 -> 2
        methodCallTable.addCall(method3, method1) // 1 -> 3

        assertFalse(methodCallTable.canCall(method2, method1))
        assertFalse(methodCallTable.canCall(method3, method1))
    }

    @Test
    fun whenCanCallWithRecursion_expectFalse() {
        assertFalse(methodCallTable.canCall(method1, method1))
    }

    @Test
    fun givenMultiLayerDependency_whenCanCall_expectFalse() {
        methodCallTable.addCall(method2, method1) // 1 -> 2
        methodCallTable.addCall(method3, method2) // 2 -> 3

        assertFalse(methodCallTable.canCall(method3, method1))
    }
}
