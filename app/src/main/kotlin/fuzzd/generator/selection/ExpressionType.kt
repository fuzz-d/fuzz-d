package fuzzd.generator.selection

enum class ExpressionType {
    CONSTRUCTOR,
    UNARY,
    MODULUS,
    BINARY,
    TERNARY,
    FUNCTION_METHOD_CALL,
    IDENTIFIER,
    INDEX,
    INDEX_ASSIGN,
    LITERAL,
}
