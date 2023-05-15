package fuzzd.generator.selection

enum class StatementType {
    ASSERT,
    ASSIGN,
    MULTI_ASSIGN,
    CLASS_INSTANTIATION,
    DECLARATION,
    FORALL,
    FOR_LOOP,
    IF,
    MATCH,
    MAP_ASSIGN,
    METHOD_CALL,
    PRINT,
    WHILE,
}

enum class AssignType {
    IDENTIFIER,
    ARRAY_INDEX,
}
