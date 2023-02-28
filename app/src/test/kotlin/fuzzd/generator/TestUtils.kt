package fuzzd.generator

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

inline fun <reified T : Throwable> expectFailure(expectedMessage: String, block: () -> Unit) {
    runCatching {
        block()
    }.onSuccess {
        fail()
    }.onFailure { throwable ->
        assertTrue { throwable is T }
        assertEquals(expectedMessage, throwable.message)
    }
}
