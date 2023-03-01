package fuzzd.generator.identifier_generator

import fuzzd.generator.ast.identifier_generator.NameGenerator.IdentifierNameGenerator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NameGeneratorTests {

    private val nameGenerator = IdentifierNameGenerator()

    @Test
    fun givenGeneratedName_whenNewValue_expectNumberIncremented() {
        // given
        val identifierName = nameGenerator.newValue()
        assertEquals("v0", identifierName) // sanity check

        // when
        val nextIdentifierName = nameGenerator.newValue()

        // expect
        assertEquals("v1", nextIdentifierName)
    }
}
