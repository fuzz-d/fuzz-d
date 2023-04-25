package fuzzd.recondition.visitor

import dafnyParser.IdentifierContext
import dafnyParser.UpperIdentifierContext

fun visitIdentifierName(identifierCtx: IdentifierContext): String = identifierCtx.IDENTIFIER().toString()

fun visitUpperIdentifierName(upperIdentifierCtx: UpperIdentifierContext): String =
    upperIdentifierCtx.UPPER_IDENTIFIER().toString()
