package fuzzd.generator

import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.error.IdentifierOnDemandException
import fuzzd.generator.context.GenerationContext
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.symbol_table.FunctionSymbolTable
import fuzzd.generator.symbol_table.GlobalSymbolTable
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GeneratorTests {

    private lateinit var generator: Generator

    private val selectionManager: SelectionManager = mockk()

    @BeforeEach
    fun setup() {
        generator = Generator(selectionManager, globalState = true, verifier = false)
    }

    @Test
    fun givenOnDemandIdentifiersDisabled_whenGenerateIdentifierWithNoneAvailable_expectIdentifierOnDemandException() {
        // given
        val context = GenerationContext(GlobalSymbolTable(), FunctionSymbolTable(), onDemandIdentifiers = false)

        // expect
        assertFailsWith<IdentifierOnDemandException> {
            generator.generateIdentifier(context, BoolType)
        }
    }
}
