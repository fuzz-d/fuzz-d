package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.probability_manager.BaseProbabilityManager
import fuzzd.generator.selection.probability_manager.ProbabilityManager
import fuzzd.generator.selection.probability_manager.RandomProbabilityManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.utils.DAFNY_BODY
import fuzzd.utils.DAFNY_GENERATED
import fuzzd.utils.DAFNY_MAIN
import fuzzd.utils.DAFNY_TYPE
import fuzzd.utils.DAFNY_WRAPPERS
import fuzzd.validator.OutputValidator
import java.io.File
import kotlin.random.Random

class FuzzRunner(private val dir: File, private val logger: Logger) {
    private val validator = OutputValidator()
    private val reconditionRunner = ReconditionRunner(dir, logger)

    fun run(seed: Long, advanced: Boolean, instrument: Boolean, run: Boolean, swarm: Boolean) {
        val generator = Generator(
            SelectionManager(
                Random(seed),
                if (swarm) RandomProbabilityManager(seed, setOf(ProbabilityManager::charType, ProbabilityManager::multisetConversion)) else BaseProbabilityManager(),
            ),
            instrument,
        )

        logger.log { "Fuzzing with seed: $seed" }
        println("Fuzzing with seed: $seed")
        println("Output being written to directory: ${dir.path}")

        // generate program
        try {
            val ast = generator.generate()

            logger.log { "Generated ast" }

            val originalWriter = OutputWriter(dir, "$DAFNY_GENERATED.$DAFNY_TYPE")
            originalWriter.write { ast }
            originalWriter.close()

            val output = reconditionRunner.run(ast, advanced)
            
            if (run) {
                // differential testing; log results
                val validationResult = validator.validateFile(dir, DAFNY_WRAPPERS, DAFNY_BODY, DAFNY_MAIN, output)
                logger.log { validationResult }
            }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Generation threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
            println(e.stackTraceToString())
        }
    }
}
