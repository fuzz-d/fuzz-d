package fuzzd.generator.symbol_table

import fuzzd.generator.ast.ClassAST
import fuzzd.generator.ast.TraitAST
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSymbolTableTests {

    private val globalSymbolTable = GlobalSymbolTable()

    @Test
    fun givenGSTWithNoParent_whenAddTrait_expectTraitStoredInCurrentFST() {
        // when
        globalSymbolTable.addTrait(TRAIT_T1)

        // expect
        val traits = globalSymbolTable.traits()
        assertEquals(setOf(TRAIT_T1), traits)
    }

    @Test
    fun givenGST_whenAddTrait_expectTraitStored() {
        // when
        globalSymbolTable.addTrait(TRAIT_T1)

        // expect
        val traits = globalSymbolTable.traits()
        assertEquals(setOf(TRAIT_T1), traits)
    }

    @Test
    fun givenGSTWithNoParent_whenAddClass_expectClassStoredInCurrentFST() {
        // when
        globalSymbolTable.addClass(CLASS_C1)

        // expect
        val classes = globalSymbolTable.classes()
        assertEquals(setOf(CLASS_C1), classes)
    }

    @Test
    fun givenGST_whenAddClass_expectClassStored() {
        // when
        globalSymbolTable.addClass(CLASS_C1)

        // expect
        assertTrue { globalSymbolTable.hasClasses() }

        val classes = globalSymbolTable.classes()
        assertEquals(setOf(CLASS_C1), classes)
    }

    @Test
    fun givenGST_whenAddClasses_expectClassesStored() {
        // when
        globalSymbolTable.addClasses(listOf(CLASS_C1, CLASS_C2))

        // expect
        assertTrue { globalSymbolTable.hasClasses() }

        val classes = globalSymbolTable.classes()
        assertEquals(setOf(CLASS_C1, CLASS_C2), classes)
    }

    companion object {
        private val CLASS_C1 = ClassAST.builder().withName("C1").build()
        private val CLASS_C2 = ClassAST.builder().withName("C2").build()
        private val TRAIT_T1 = TraitAST.builder().withName("T1").build()
    }
}
