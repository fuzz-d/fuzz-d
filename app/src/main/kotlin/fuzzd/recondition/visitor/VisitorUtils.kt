package fuzzd.recondition.visitor

import dafnyParser.IdentifierContext

fun visitIdentifierName(identifierCtx: IdentifierContext): String = identifierCtx.IDENTIFIER().toString()
