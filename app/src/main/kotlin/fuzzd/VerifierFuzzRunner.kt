package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.selection.SelectionManager
import fuzzd.generator.selection.probability_manager.BaseProbabilityManager
import fuzzd.generator.selection.probability_manager.VerifierProbabilityManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.mutation.VerifierAnnotationMutator
import fuzzd.utils.DAFNY_GENERATED
import fuzzd.utils.DAFNY_MAIN
import fuzzd.utils.DAFNY_TYPE
import fuzzd.utils.WRAPPER_FUNCTIONS
import fuzzd.validator.OutputValidator
import java.io.File
import kotlin.random.Random

class VerifierFuzzRunner(private val dir: File, private val logger: Logger) {
    private val reconditionRunner = ReconditionRunner(dir, logger)

    fun run(seed: Long, run: Boolean = true) {
        val random = Random(seed)
        val probabilityManager = VerifierProbabilityManager(BaseProbabilityManager())
        val selectionManager = SelectionManager(random, probabilityManager)
        val generator = Generator(selectionManager, globalState = false, verifier = true)

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

            if (run) {
                val outputValidator = OutputValidator()
                // verify original main.dfy
                logger.log { "==========================================================" }
                logger.log { "Verifying original main.dfy file" }
                val result = outputValidator.verifyFiles(dir, listOf(DAFNY_MAIN))
                logger.log { result.first() }
                when (result.first().exitCode) {
                    0 -> {
                        logger.log { "Verification of main.dfy succeeded. Creating mutants..." }

                        val mutantFiles = (1..VERIFIER_PROGRAM_MUTANTS).map { i ->
                            val mutantSeed = random.nextLong()
                            val mutantSelectionManager = SelectionManager(Random(mutantSeed), probabilityManager)
                            val mutator = VerifierAnnotationMutator(mutantSelectionManager)

                            val mutantFileName = "mutated$i"
                            val mutantWriter = OutputWriter(dir, "$mutantFileName.dfy")
                            WRAPPER_FUNCTIONS.forEach { wrapper -> mutantWriter.write { "$wrapper\n" } }
                            mutantWriter.write { mutator.mutateDafny(output.second) }
                            mutantWriter.close()

                            mutantFileName
                        }

                        logger.log { "Created mutants. Verifying..." }
                        val mutantResults = outputValidator.verifyFiles(dir, mutantFiles)

                        mutantFiles.zip(mutantResults).forEach { (file, result) ->
                            logger.log { "==========================================================" }
                            logger.log { "File $file results: \n" }
                            logger.log { result }
                        }

                        if (mutantResults.any { it.exitCode == 0 }) {
                            logger.log { "Invalid verification detected" }
                        }
                    }

                    1 -> logger.log { "Verification of main.dfy failed. Exiting." }
                    else -> logger.log { "Verification of main.dfy timed out. Exiting." }
                }
            }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Verifier fuzzing threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
            println(e.stackTraceToString())
        }
    }

    companion object {
        private const val VERIFIER_PROGRAM_MUTANTS = 5
    }
}
