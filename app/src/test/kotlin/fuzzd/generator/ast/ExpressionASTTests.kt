package fuzzd.generator.ast

import fuzzd.generator.ast.ExpressionAST.ArrayIndexAST
import fuzzd.generator.ast.ExpressionAST.ArrayLengthAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.CharacterLiteralAST
import fuzzd.generator.ast.ExpressionAST.ClassInstantiationAST
import fuzzd.generator.ast.ExpressionAST.FunctionMethodCallAST
import fuzzd.generator.ast.ExpressionAST.IdentifierAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.NonVoidMethodCallAST
import fuzzd.generator.ast.ExpressionAST.RealLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.Type.BoolType
import fuzzd.generator.ast.Type.ConstructorType.ArrayType
import fuzzd.generator.ast.Type.IntType
import fuzzd.generator.ast.error.InvalidInputException
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import fuzzd.generator.ast.operators.UnaryOperator.NotOperator
import fuzzd.utils.ABSOLUTE
import fuzzd.utils.SAFE_DIVISION_INT
import fuzzd.utils.SAFE_MODULO_INT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExpressionASTTests {

    @Nested
    inner class ClassInstantiationTests {

        @Test
        fun givenClassInstantiationAST_whenInitWithCorrectParams_expectSuccessfulInit() {
            // given
            val classFields = setOf(IdentifierAST("cf1", BoolType), IdentifierAST("cf2", IntType))
            val clazz = ClassAST.builder().withName("C1").withFields(classFields).build()

            val params = listOf(BooleanLiteralAST(false), IntegerLiteralAST(50))

            // when
            ClassInstantiationAST(clazz, params)

            // expect nothing
        }

        @Test
        fun givenClassInstantiationAST_whenInitWithTooManyParams_expectInvalidInputError() {
            // given
            val classFields = setOf(IdentifierAST("cf1", BoolType), IdentifierAST("cf2", IntType))
            val clazz = ClassAST.builder().withName("C1").withFields(classFields).build()

            val params = listOf(BooleanLiteralAST(false), IntegerLiteralAST(50), BooleanLiteralAST(true))

            runCatching {
                ClassInstantiationAST(clazz, params)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue(throwable is InvalidInputException)
                assertEquals(
                    "Number of parameters for context {constructor call for C1} doesn't match. Expected 2, got 3",
                    throwable.message,
                )
            }
        }

        @Test
        fun givenClassInstantiationAST_whenInitWithTooFewParams_expectInvalidInputError() {
            // given
            val classFields = setOf(IdentifierAST("cf1", BoolType), IdentifierAST("cf2", IntType))
            val clazz = ClassAST.builder().withName("C1").withFields(classFields).build()

            val params = listOf(BooleanLiteralAST(false))

            runCatching {
                ClassInstantiationAST(clazz, params)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue(throwable is InvalidInputException)
                assertEquals(
                    "Number of parameters for context {constructor call for C1} doesn't match. Expected 2, got 1",
                    throwable.message,
                )
            }
        }

        @Test
        fun givenClassInstantiationAST_whenInitWithIncorrectParameterType_expectInvalidInputError() {
            // given
            val classFields = setOf(IdentifierAST("cf1", BoolType), IdentifierAST("cf2", IntType))
            val clazz = ClassAST.builder().withName("C1").withFields(classFields).build()

            val params = listOf(IntegerLiteralAST(42), IntegerLiteralAST(52))

            runCatching {
                ClassInstantiationAST(clazz, params)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue(throwable is InvalidInputException)
                assertEquals(
                    "Parameter type mismatch for parameter 0 in context {constructor call for C1}. Expected bool, got int",
                    throwable.message,
                )
            }
        }
    }

    @Nested
    inner class FunctionMethodCallTests {

        @Test
        fun givenFunctionMethod_whenMethodCallWithCorrectParams_expectSuccessfulInit() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val body = IntegerLiteralAST(37481)
            val method = FunctionMethodAST(FunctionMethodSignatureAST("fm1", IntType, params), body)

            // when
            val callParams = listOf(IntegerLiteralAST(3), BooleanLiteralAST(false))
            FunctionMethodCallAST(method, callParams)

            // expect success
        }

        @Test
        fun givenFunctionMethod_whenMethodCallWithTooManyParams_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val body = IntegerLiteralAST(37481)
            val method = FunctionMethodAST(FunctionMethodSignatureAST("fm1", IntType, params), body)

            val callParams = listOf(IntegerLiteralAST(3), BooleanLiteralAST(false), IntegerLiteralAST(135))

            // expect
            runCatching {
                FunctionMethodCallAST(method, callParams)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue { throwable is InvalidInputException }
                assertEquals(
                    throwable.message,
                    "Number of parameters for context {function method call to fm1} doesn't match. Expected 2, got 3",
                )
            }
        }

        @Test
        fun givenFunctionMethod_whenMethodCallWithTooFewParams_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val body = IntegerLiteralAST(37481)
            val method = FunctionMethodAST(FunctionMethodSignatureAST("fm1", IntType, params), body)

            val callParams = listOf<ExpressionAST>()

            // expect
            runCatching {
                FunctionMethodCallAST(method, callParams)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue { throwable is InvalidInputException }
                assertEquals(
                    throwable.message,
                    "Number of parameters for context {function method call to fm1} doesn't match. Expected 2, got 0",
                )
            }
        }

        @Test
        fun givenFunctionMethod_whenMethodCallWithIncorrectParamTypes_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val body = IntegerLiteralAST(37481)
            val method = FunctionMethodAST(FunctionMethodSignatureAST("fm1", IntType, params), body)

            val callParams = listOf<ExpressionAST>(IdentifierAST("p1", BoolType), IdentifierAST("p2", IntType))

            // expect
            runCatching {
                FunctionMethodCallAST(method, callParams)
            }.onSuccess {
                fail()
            }.onFailure { throwable ->
                assertTrue { throwable is InvalidInputException }
                assertEquals(
                    throwable.message,
                    "Parameter type mismatch for parameter 0 in context {function method call to fm1}. Expected int, got bool",
                )
            }
        }
    }

    @Nested
    inner class NonVoidMethodCallTests {

        @Test
        fun givenMethod_whenMethodCallWithCorrectParams_expectSuccessfulInit() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val returns = listOf(IdentifierAST("r1", IntType), IdentifierAST("r2", IntType))
            val method = MethodAST("m1", params, returns)

            // when
            val callParams = listOf(IntegerLiteralAST(3), BooleanLiteralAST(false))
            NonVoidMethodCallAST(method, callParams)

            // expect success
        }

        @Test
        fun givenMethod_whenMethodCallWithTooManyParams_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val returns = listOf(IdentifierAST("r1", IntType), IdentifierAST("r2", IntType))
            val method = MethodAST("m1", params, returns)

            // when
            val callParams = listOf(IntegerLiteralAST(3), BooleanLiteralAST(false), IntegerLiteralAST(13))
            assertFailsWith<InvalidInputException>("Number of parameters for call to m1 doesn't match. Expected 2, Got 3") {
                NonVoidMethodCallAST(method, callParams)
            }
        }

        @Test
        fun givenMethod_whenMethodCallWithTooFewParams_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val returns = listOf(IdentifierAST("r1", IntType), IdentifierAST("r2", IntType))
            val method = MethodAST("m1", params, returns)

            // when
            val callParams = listOf<ExpressionAST>()
            assertFailsWith<InvalidInputException>("Number of parameters for call to m1 doesn't match. Expected 2, Got 0") {
                NonVoidMethodCallAST(method, callParams)
            }
        }

        @Test
        fun givenMethod_whenMethodCallWithIncorrectTypeParams_expectError() {
            // given
            val params = listOf(IdentifierAST("p1", IntType), IdentifierAST("p2", BoolType))
            val returns = listOf(IdentifierAST("r1", IntType), IdentifierAST("r2", IntType))
            val method = MethodAST("m1", params, returns)

            // when
            val callParams = listOf(BooleanLiteralAST(false), IntegerLiteralAST(13))
            assertFailsWith<InvalidInputException>("Method call parameter type mismatch for parameter 0. Expected int, got bool") {
                NonVoidMethodCallAST(method, callParams)
            }
        }
    }

    @Nested
    inner class TernaryExpressionASTTests {
        @Test
        fun givenValidConditionAndBranches_whenInit_expectSuccess() {
            // given
            val condition = BooleanLiteralAST(true)
            val ifBranch = IntegerLiteralAST(53)
            val elseBranch = IntegerLiteralAST(43)

            // when
            TernaryExpressionAST(condition, ifBranch, elseBranch)

            // expect success
        }

        @Test
        fun givenValidConditionAndBranches_whenType_expectCorrectType() {
            // given
            val condition = BooleanLiteralAST(true)
            val ifBranch = IntegerLiteralAST(53)
            val elseBranch = IntegerLiteralAST(43)

            // when
            val expr = TernaryExpressionAST(condition, ifBranch, elseBranch)

            // expect valid type
            assertEquals(IntType, expr.type())
        }

        @Test
        fun givenNonBoolCondition_whenInit_expectError() {
            // given
            val condition = IntegerLiteralAST(314)
            val ifBranch = IntegerLiteralAST(31)
            val elseBranch = IntegerLiteralAST(43)

            // expect
            assertFailsWith<InvalidInputException>("Invalid input type for ternary expression condition. Got int") {
                TernaryExpressionAST(condition, ifBranch, elseBranch)
            }
        }

        @Test
        fun givenBranchesOfDifferentTypes_whenInit_expectError() {
            // given
            val condition = BooleanLiteralAST(false)
            val ifBranch = IntegerLiteralAST(31)
            val elseBranch = CharacterLiteralAST('c')

            // expect
            assertFailsWith<InvalidInputException>("Ternary expression branches have different types. If branch: int. Else branch: char") {
                TernaryExpressionAST(condition, ifBranch, elseBranch)
            }
        }
    }

    @Nested
    inner class UnaryExpressionASTTests {
        @Test
        fun givenBinaryExpression_whenToString_expectWrapExpr() {
            // given
            val bexp = BinaryExpressionAST(BooleanLiteralAST(false), ConjunctionOperator, BooleanLiteralAST(true))
            val operator = NotOperator
            val expr = UnaryExpressionAST(bexp, operator)

            // when
            val str = expr.toString()

            // expect
            assertEquals("$operator($bexp)", str)
        }
    }

    @Nested
    inner class BinaryExpressionASTTests {
        @Test
        fun givenBinaryExpressionWithBooleanBinaryChild_whenToString_expectParenWrap() {
            // given
            val lhs = BinaryExpressionAST(BooleanLiteralAST(false), ConjunctionOperator, BooleanLiteralAST(true))
            val operator = DisjunctionOperator
            val rhs = BooleanLiteralAST(false)
            val bexp = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val str = bexp.toString()

            // expect
            assertEquals("($lhs) $operator $rhs", str)
        }

        @Test
        fun givenBinaryExpressionWithIntBinaryChild_whenToString_expectNoParenWrap() {
            // given
            val lhs = BinaryExpressionAST(IntegerLiteralAST("123"), AdditionOperator, IntegerLiteralAST("124"))
            val operator = SubtractionOperator
            val rhs = IntegerLiteralAST("1234")

            val bexp = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val str = bexp.toString()

            // expect
            assertEquals("$lhs $operator $rhs", str)
        }

        @Test
        fun givenBinaryExpressionWithIntDivision_whenMakeSafe_expectSafeWrapper() {
            // given
            val lhs = IntegerLiteralAST("123")
            val operator = DivisionOperator
            val rhs = IntegerLiteralAST("234")
            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = FunctionMethodCallAST(SAFE_DIVISION_INT, listOf(lhs, rhs))
            assertEquals(expected.toString(), safe.toString())
        }

//        @Test
//        fun givenBinaryExpressionWithRealDivision_whenMakeSafe_expectNonZeroWrapper() {
//            // given
//            val lhs = RealLiteralAST("123.3")
//            val operator = DivisionOperator
//            val rhs = RealLiteralAST("234.3")
//            val expr = BinaryExpressionAST(lhs, operator, rhs)
//
//            // when
//            val safe = expr.makeSafe()
//
//            // expect
//            val expected = FunctionMethodCallAST(SAFE_DIVISION_REAL, listOf(lhs, rhs))
//            assertEquals(expected.toString(), safe.toString())
//        }

        @Test
        fun givenBinaryExpressionWithModulo_whenMakeSafe_expectSafeWrapper() {
            // given
            val lhs = IntegerLiteralAST("1234")
            val operator = ModuloOperator
            val rhs = IntegerLiteralAST("2345")
            val expr = BinaryExpressionAST(lhs, operator, rhs)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = FunctionMethodCallAST(SAFE_MODULO_INT, listOf(lhs, rhs))

            assertEquals(expected.toString(), safe.toString())
        }

//        @Test
//        fun givenBinaryExpressionWithCharSubtraction_whenMakeSafe_expectSafeCharSubtractionWrapper() {
//            // given
//            val lhs = CharacterLiteralAST('B')
//            val operator = SubtractionOperator
//            val rhs = CharacterLiteralAST('a')
//
//            val expr = BinaryExpressionAST(lhs, operator, rhs)
//
//            // when
//            val safe = expr.makeSafe()
//
//            // expect
//            val expected = FunctionMethodCallAST(SAFE_SUBTRACT_CHAR, listOf(lhs, rhs))
//            assertEquals(expected.toString(), safe.toString())
//        }
    }

    @Nested
    inner class IdentifierASTTests {
        @Test
        fun givenArrayIndexAST_whenMakeSafe_expectAbsoluteWrapperAndModulo() {
            // given
            val array = IdentifierAST("a", ArrayType(IntType))
            val index = IntegerLiteralAST("43")

            val expr = ArrayIndexAST(array, index)

            // when
            val safe = expr.makeSafe()

            // expect
            val expected = ArrayIndexAST(
                array,
                BinaryExpressionAST(
                    FunctionMethodCallAST(ABSOLUTE, listOf(index)),
                    ModuloOperator,
                    ArrayLengthAST(array),
                ),
            )

            assertEquals(expected.toString(), safe.toString())
        }
    }

    @Nested
    inner class IntegerLiteralASTTests {
        @Test
        fun givenValidIntegerValue_whenCreateIntegerLiteralAST_expectSuccessfulInit() {
            // given
            val value = "123456196"

            // when
            IntegerLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidIntegerValue_whenHexToString_expectHexString() {
            // given
            val value = "258"

            // when
            val str = IntegerLiteralAST(value, true).toString()

            // expect
            assertEquals("0x102", str)
        }

        @Test
        fun givenValidNegativeIntegerValue_whenHexToString_expectHexString() {
            // given
            val value = "-258"

            // when
            val str = IntegerLiteralAST(value, true).toString()

            // expect
            assertEquals("-0x102", str)
        }
    }

    @Nested
    inner class RealLiteralASTTests {
        @Test
        fun givenValidRealValue_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "123.53195"

            // when
            RealLiteralAST(value)

            // expect nothing
        }

        @Test
        fun givenValidNegativeRealValue_whenCreateRealLiteralAST_expectSuccessfulInit() {
            // given
            val value = "-1234951.95138"

            // when
            RealLiteralAST(value)

            // expect nothing
        }
    }
}
