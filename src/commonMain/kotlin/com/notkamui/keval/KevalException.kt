package com.notkamui.keval

/**
 * Generic Keval Exception
 *
 * @param message is the message to display in the stacktrace
 */
sealed class KevalException(message: String) : Exception(message)

/**
 * Invalid Expression Exception, is thrown when the expression is considered invalid (i.e. Mismatched parenthesis or missing operands)
 *
 * @property expression is the invalid expression
 * @property position is the estimated position of the error
 */
open class KevalInvalidExpressionException internal constructor(
    val expression: String,
    val position: Int,
    extraMessage: String = ""
) : KevalException(
    "Invalid expression at position $position in $expression${
        if (extraMessage.isNotBlank()) ", $extraMessage" else ""
    }"
)

/**
 * Invalid Operator Exception, is thrown when an invalid/unknown operator is found
 *
 * @property invalidSymbol is the given invalid operator
 * @param expression is the invalid expression
 * @param position is the estimated position of the error
 */
class KevalInvalidSymbolException internal constructor(
    val invalidSymbol: String,
    expression: String,
    position: Int,
    message: String = ""
) : KevalInvalidExpressionException(expression, position, message)

/**
 * Zero Division Exception, is thrown when a zero division occurs (i.e. x/0, x%0)
 */
class KevalZeroDivisionException : KevalException("Division by zero")

/**
 * DSL Exception, is thrown when a required field isn't defined
 *
 * @param what is the name of the undefined field
 */
class KevalDSLException internal constructor(what: String) :
    KevalException("All required fields must be properly defined: $what")
