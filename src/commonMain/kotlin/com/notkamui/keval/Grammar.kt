package com.notkamui.keval

private fun isUnaryOrBothPrefix(token: String, operators: Map<String, KevalOperator>): Boolean =
    operators[token] is KevalUnaryOperator && (operators[token] as KevalUnaryOperator).isPrefix
    || operators[token] is KevalBothOperator && (operators[token] as KevalBothOperator).unary.isPrefix

private fun isUnaryOrBothPostfix(token: String, operators: Map<String, KevalOperator>): Boolean =
    operators[token] is KevalUnaryOperator && !(operators[token] as KevalUnaryOperator).isPrefix
    || operators[token] is KevalBothOperator && !(operators[token] as KevalBothOperator).unary.isPrefix

private fun getBinaryOperator(token: String, operators: Map<String, KevalOperator>): KevalBinaryOperator =
    if (operators[token] is KevalBothOperator) {
        (operators[token] as KevalBothOperator).binary
    } else {
        operators[token] as KevalBinaryOperator
    }

private fun getUnaryOperator(token: String, operators: Map<String, KevalOperator>): KevalUnaryOperator =
    if (operators[token] is KevalBothOperator) {
        (operators[token] as KevalBothOperator).unary
    } else {
        operators[token] as KevalUnaryOperator
    }

internal class Parser(private val tokens: Iterator<String>, private val operators: Map<String, KevalOperator>) {
    private var currentToken: String? = tokens.next()

    private fun consume(expected: String) {
        if (currentToken != expected) {
            throw KevalInvalidExpressionException(currentToken ?: "", -1)
        }
        currentToken = if (tokens.hasNext()) tokens.next() else null
    }

    private fun expression(minPrecedence: Int = 0): Node {
        var node = primary()
        while (currentToken != null && (operators[currentToken!!] is KevalBinaryOperator || operators[currentToken!!] is KevalBothOperator)) {
            val op = getBinaryOperator(currentToken!!, operators)
            if (op.precedence < minPrecedence) break
            consume(currentToken!!)
            val rightAssociativity = if (op.isLeftAssociative) 1 else 0
            node = BinaryOperatorNode(node, op.implementation, expression(op.precedence + rightAssociativity))
        }
        if (currentToken != null && isUnaryOrBothPostfix(currentToken!!, operators)) {
            val op = getUnaryOperator(currentToken!!, operators)
            consume(currentToken!!)
            node = UnaryOperatorNode(op.implementation, node)
        }
        return node
    }

    private fun primary(): Node {
        if (currentToken != null && isUnaryOrBothPrefix(currentToken!!, operators)) {
            val op = getUnaryOperator(currentToken!!, operators)
            consume(currentToken!!)
            return UnaryOperatorNode(op.implementation, primary())
        } else if (currentToken == "(") {
            consume("(")
            val node = expression()
            consume(")")
            return node
        } else if (operators.containsKey(currentToken)) {
            val op = operators[currentToken!!]
            if (op is KevalFunction) {
                val functionName = currentToken!!
                consume(functionName)
                consume("(")
                val args = mutableListOf<Node>()
                while (currentToken != ")") {
                    args.add(expression())
                    if (currentToken == ",") {
                        consume(",")
                    }
                }
                consume(")")
                if (args.size != op.arity) {
                    throw KevalInvalidExpressionException(currentToken ?: "", -1)
                }
                return FunctionNode(op.implementation, args)
            } else if (op is KevalConstant) {
                val constantValue = op.value
                consume(currentToken!!)
                return ValueNode(constantValue)
            }
        }
        val node = ValueNode(currentToken!!.toDouble())
        consume(currentToken!!)
        return node
    }

    fun parse(): Node = expression()
}

/**
 * Converts an infix mathematical expression into an abstract syntax tree,
 * The operators that are supported are defined in the operators map, which each have a precedence and associativity.
 *
 * @receiver the string to convert
 * @return the abstract syntax tree
 * @throws KevalInvalidSymbolException if the expression contains an invalid symbol
 * @throws KevalInvalidExpressionException if the expression is invalid (i.e. mismatched parenthesis, missing operand, or empty expression)
 */
internal fun String.toAST(operators: Map<String, KevalOperator>): Node {
    if (this.replace("""[()]""".toRegex(), "").isBlank())
        throw KevalInvalidExpressionException("", -1)

    val tokens = this.tokenize(operators)
    val tokensToString = tokens.joinToString("")

    val parser = Parser(tokens.iterator(), operators)
    return parser.parse()
}
