package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.probability_manager.BaseProbabilityManager
import fuzzd.generator.selection.probability_manager.VerifierProbabilityManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.mutation.VerifierAnnotationMutator
import fuzzd.utils.DAFNY_GENERATED
import fuzzd.utils.DAFNY_TYPE
import java.io.File
import kotlin.random.Random

class VerifierFuzzRunner(private val dir: File, private val logger: Logger) {
    private val reconditionRunner = ReconditionRunner(dir, logger)

    fun run(seed: Long, run: Boolean = true) {
        val probabilityManager = VerifierProbabilityManager(BaseProbabilityManager())
        val selectionManager = SelectionManager(Random(seed), probabilityManager)
        val generator = Generator(selectionManager, globalState = false, verifier = true)
        val mutator = VerifierAnnotationMutator(selectionManager)

        logger.log { "Verifier Fuzzing with seed: $seed" }
        println("Verifier Fuzzing with seed: $seed")
        println("Output being written to directory: ${dir.path}")

        try {
            val ast = generator.generate()

            logger.log { "Generated ast" }

            val originalWriter = OutputWriter(dir, "$DAFNY_GENERATED.$DAFNY_TYPE")
            originalWriter.write { ast }
            originalWriter.close()

            val output = reconditionRunner.run(ast, false)

            val mutatedWriter = OutputWriter(dir, "mutated1.$DAFNY_TYPE")
            mutatedWriter.write { mutator.mutateDafny(output.second) }
            mutatedWriter.close()

            if (run) {
                TODO()
            }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Verifier fuzzing threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
            println(e.stackTraceToString())
        }
    }
}
