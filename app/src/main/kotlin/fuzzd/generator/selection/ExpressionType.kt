package fuzzd.generator.selection

enum class ExpressionType {
    COMPREHENSION,
    CONSTRUCTOR,
    UNARY,
    MODULUS,
    MULTISET_CONVERSION,
    BINARY,
    TERNARY,
    MATCH,
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
    STRING,
    DATATYPE,
}

enum class ArrayInitType {
    COMPREHENSION,
    DEFAULT,
    VALUE,
}
