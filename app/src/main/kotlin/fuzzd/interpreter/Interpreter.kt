package fuzzd.interpreter

import fuzzd.generator.ast.ExpressionAST
import fuzzd.generator.ast.ExpressionAST.BinaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.BooleanLiteralAST
import fuzzd.generator.ast.ExpressionAST.IntegerLiteralAST
import fuzzd.generator.ast.ExpressionAST.MapConstructorAST
import fuzzd.generator.ast.ExpressionAST.ModulusExpressionAST
import fuzzd.generator.ast.ExpressionAST.MultisetConversionAST
import fuzzd.generator.ast.ExpressionAST.SetDisplayAST
import fuzzd.generator.ast.ExpressionAST.StringLiteralAST
import fuzzd.generator.ast.ExpressionAST.TernaryExpressionAST
import fuzzd.generator.ast.ExpressionAST.UnaryExpressionAST
import fuzzd.generator.ast.operators.BinaryOperator.AdditionOperator
import fuzzd.generator.ast.operators.BinaryOperator.AntiMembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ConjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureEqualityOperator
import fuzzd.generator.ast.operators.BinaryOperator.DataStructureInequalityOperator
import fuzzd.generator.ast.operators.BinaryOperator.DifferenceOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjointOperator
import fuzzd.generator.ast.operators.BinaryOperator.DisjunctionOperator
import fuzzd.generator.ast.operators.BinaryOperator.DivisionOperator
import fuzzd.generator.ast.operators.BinaryOperator.EqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.GreaterThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.IffOperator
import fuzzd.generator.ast.operators.BinaryOperator.ImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.IntersectionOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanEqualOperator
import fuzzd.generator.ast.operators.BinaryOperator.LessThanOperator
import fuzzd.generator.ast.operators.BinaryOperator.MembershipOperator
import fuzzd.generator.ast.operators.BinaryOperator.ModuloOperator
import fuzzd.generator.ast.operators.BinaryOperator.MultiplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.NotEqualsOperator
import fuzzd.generator.ast.operators.BinaryOperator.ProperSubsetOperator
import fuzzd.generator.ast.operators.BinaryOperator.ProperSupersetOperator
import fuzzd.generator.ast.operators.BinaryOperator.ReverseImplicationOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubsetOperator
import fuzzd.generator.ast.operators.BinaryOperator.SubtractionOperator
import fuzzd.generator.ast.operators.BinaryOperator.SupersetOperator
import fuzzd.generator.ast.operators.BinaryOperator.UnionOperator
import fuzzd.generator.ast.operators.UnaryOperator.NegationOperator
import fuzzd.interpreter.value.Value
import fuzzd.interpreter.value.Value.BoolValue
import fuzzd.interpreter.value.Value.DataStructureValue
import fuzzd.interpreter.value.Value.IntValue
import fuzzd.interpreter.value.Value.MapValue
import fuzzd.interpreter.value.Value.MultisetValue
import fuzzd.interpreter.value.Value.SetValue
import fuzzd.interpreter.value.Value.StringValue
import fuzzd.utils.toMultiset

class Interpreter : ASTInterpreter {

    /* ============================== EXPRESSIONS ============================ */
    override fun interpretExpression(expression: ExpressionAST): Value =

        when (expression) {
            is BinaryExpressionAST -> interpretBinaryExpression(expression)
            is TernaryExpressionAST -> interpretTernaryExpression(expression)
            is UnaryExpressionAST -> interpretUnaryExpression(expression)
            is ModulusExpressionAST -> interpretModulus(expression)
            is MultisetConversionAST -> interpretMultisetConversion(expression)
            is SetDisplayAST -> interpretSetDisplay(expression)
            is MapConstructorAST -> interpretMapConstructor(expression)
            is StringLiteralAST -> interpretStringLiteral(expression)
            is IntegerLiteralAST -> interpretIntegerLiteral(expression)
            is BooleanLiteralAST -> interpretBooleanLiteral(expression)
            else -> throw UnsupportedOperationException()
        }

    override fun interpretBinaryExpression(binaryExpression: BinaryExpressionAST): Value {
        val lhs = interpretExpression(binaryExpression.expr1)
        val rhs = interpretExpression(binaryExpression.expr2)

        return when (binaryExpression.operator) {
            IffOperator -> (lhs as BoolValue).iff(rhs as BoolValue)
            ImplicationOperator -> (lhs as BoolValue).impl(rhs as BoolValue)
            ReverseImplicationOperator -> (lhs as BoolValue).rimpl(rhs as BoolValue)
            ConjunctionOperator -> (lhs as BoolValue).and(rhs as BoolValue)
            DisjunctionOperator -> (lhs as BoolValue).or(rhs as BoolValue)
            LessThanOperator -> (lhs as IntValue).lessThan(rhs as IntValue)
            LessThanEqualOperator -> (lhs as IntValue).lessThanEquals(rhs as IntValue)
            GreaterThanEqualOperator -> (lhs as IntValue).greaterThanEquals(rhs as IntValue)
            GreaterThanOperator -> (lhs as IntValue).greaterThan(rhs as IntValue)
            EqualsOperator, DataStructureEqualityOperator -> BoolValue(lhs == rhs)
            NotEqualsOperator, DataStructureInequalityOperator -> BoolValue(lhs != rhs)
            AdditionOperator -> (lhs as IntValue).plus(rhs as IntValue)
            SubtractionOperator -> (lhs as IntValue).subtract(rhs as IntValue)
            MultiplicationOperator -> (lhs as IntValue).multiply(rhs as IntValue)
            DivisionOperator -> (lhs as IntValue).divide(rhs as IntValue)
            ModuloOperator -> (lhs as IntValue).modulo(rhs as IntValue)
            MembershipOperator -> (rhs as DataStructureValue).contains(lhs)
            AntiMembershipOperator -> (rhs as DataStructureValue).notContains(lhs)
            ProperSubsetOperator -> interpretProperSubset(lhs, rhs)
            SubsetOperator -> interpretSubset(lhs, rhs)
            SupersetOperator -> interpretSuperset(lhs, rhs)
            ProperSupersetOperator -> interpretProperSuperset(lhs, rhs)
            DisjointOperator -> interpretDisjoint(lhs, rhs)
            UnionOperator -> interpretUnion(lhs, rhs)
            DifferenceOperator -> interpretDifference(lhs, rhs)
            IntersectionOperator -> interpretIntersection(lhs, rhs)
        }
    }

    private fun interpretProperSubset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.properSubsetOf(rhs as MultisetValue)
        is SetValue -> lhs.properSubsetOf(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretSubset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.subsetOf(rhs as MultisetValue)
        is SetValue -> lhs.subsetOf(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretSuperset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.supersetOf(rhs as MultisetValue)
        is SetValue -> lhs.supersetOf(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretProperSuperset(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.properSupersetOf(rhs as MultisetValue)
        is SetValue -> lhs.properSupersetOf(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretDisjoint(lhs: Value, rhs: Value): BoolValue = when (lhs) {
        is MultisetValue -> lhs.disjoint(rhs as MultisetValue)
        is SetValue -> lhs.disjoint(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretUnion(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.union(rhs as MultisetValue)
        is SetValue -> lhs.union(rhs as SetValue)
        is MapValue -> lhs.union(rhs as MapValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretDifference(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.difference(rhs as MultisetValue)
        is SetValue -> lhs.difference(rhs as SetValue)
        is MapValue -> lhs.difference(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    private fun interpretIntersection(lhs: Value, rhs: Value): Value = when (lhs) {
        is MultisetValue -> lhs.intersect(rhs as MultisetValue)
        is SetValue -> lhs.intersect(rhs as SetValue)
        else -> throw UnsupportedOperationException()
    }

    override fun interpretTernaryExpression(ternaryExpression: TernaryExpressionAST): Value {
        val condition = interpretExpression(ternaryExpression.condition)
        return if ((condition as BoolValue).value) {
            interpretExpression(ternaryExpression.ifBranch)
        } else {
            interpretExpression(ternaryExpression.elseBranch)
        }
    }

    override fun interpretUnaryExpression(unaryExpression: UnaryExpressionAST): Value {
        val exprValue = interpretExpression(unaryExpression.expr)
        return if (unaryExpression.operator == NegationOperator) {
            (exprValue as IntValue).negate()
        } else {
            (exprValue as BoolValue).not()
        }
    }

    override fun interpretModulus(modulus: ModulusExpressionAST): Value =
        (interpretExpression(modulus.expr) as DataStructureValue).modulus()

    override fun interpretMultisetConversion(multisetConversion: MultisetConversionAST): Value {
        TODO("Not yet implemented")
    }

    override fun interpretSetDisplay(setDisplay: SetDisplayAST): Value {
        val values = setDisplay.exprs.map(this::interpretExpression)
        return if (setDisplay.isMultiset) SetValue(values.toSet()) else MultisetValue(values.toMultiset())
    }

    override fun interpretMapConstructor(mapConstructor: MapConstructorAST): Value {
        val map = mutableMapOf<Value, Value>()
        mapConstructor.assignments.forEach { (k, v) ->
            val key = interpretExpression(k)
            val value = interpretExpression(v)
            map[key] = value
        }
        return MapValue(map)
    }

    override fun interpretStringLiteral(stringLiteral: StringLiteralAST): StringValue = StringValue(stringLiteral.value)

    override fun interpretIntegerLiteral(intLiteral: IntegerLiteralAST): IntValue = IntValue(intLiteral.value.toLong())

    override fun interpretBooleanLiteral(boolLiteral: BooleanLiteralAST): BoolValue = BoolValue(boolLiteral.value)
}
