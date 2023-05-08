package fuzzd.generator.selection

enum class StatementType {
    ASSIGN,
    CLASS_INSTANTIATION,
    DECLARATION,
    FORALL,
    IF,
    MATCH,
    MAP_ASSIGN,
    METHOD_CALL,
    PRINT,
    WHILE,
}

enum class AssignType {
    IDENTIFIER,
    ARRAY_INDEX
}
