package fuzzd.generator.selection

enum class ExpressionType {
    CONSTRUCTOR,
    UNARY,
    MODULUS,
    MULTISET_CONVERSION,
    BINARY,
    TERNARY,
    FUNCTION_METHOD_CALL,
    IDENTIFIER,
    INDEX,
    INDEX_ASSIGN,
    LITERAL,
}

enum class IndexType {
    ARRAY,
    MAP,
    MULTISET,
    SEQUENCE,
    STRING
}
