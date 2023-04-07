package fuzzd.generator.selection

enum class ExpressionType {
    CONSTRUCTOR,
    UNARY,
    BINARY,
    TERNARY,
    FUNCTION_METHOD_CALL,
    IDENTIFIER,
    MAP_INDEX,
    MAP_INDEX_ASSIGN,
    LITERAL,
}
