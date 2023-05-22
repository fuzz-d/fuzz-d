package fuzzd.generator.symbol_table

import fuzzd.generator.ast.MethodSignatureAST
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DependencyTableTests {

    private lateinit var dependencyTable: DependencyTable<MethodSignatureAST>
    private val method1 = MethodSignatureAST("m1", listOf(), listOf(), mutableListOf())
    private val method2 = MethodSignatureAST("m2", listOf(), listOf(), mutableListOf())
    private val method3 = MethodSignatureAST("m3", listOf(), listOf(), mutableListOf())

    @BeforeEach
    fun setup() {
        dependencyTable = DependencyTable()
    }

    @Test
    fun givenOneLayerDependency_whenCanUseDependency_expectFalse() {
        dependencyTable.addDependency(method2, method1) // 1 -> 2
        dependencyTable.addDependency(method3, method1) // 1 -> 3

        assertFalse(dependencyTable.canUseDependency(method2, method1))
        assertFalse(dependencyTable.canUseDependency(method3, method1))
    }

    @Test
    fun whenCanUseDependencyWithRecursion_expectFalse() {
        assertFalse(dependencyTable.canUseDependency(method1, method1))
    }

    @Test
    fun givenMultiLayerDependency_whenCanUseDependency_expectFalse() {
        dependencyTable.addDependency(method2, method1) // 1 -> 2
        dependencyTable.addDependency(method3, method2) // 2 -> 3

        assertFalse(dependencyTable.canUseDependency(method3, method1))
    }
}
