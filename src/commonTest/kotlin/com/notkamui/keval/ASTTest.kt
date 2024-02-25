package com.notkamui.keval

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests on AbstractSyntaxTree
 */
class ASTTest {
    /**
     * Tests Node.eval()
     */
    @Test
    fun simpleEvalTest() {
        val operators = KevalDSL.DEFAULT_RESOURCES
        val plus = operators["+"] as? KevalBinaryOperator
        val ast: Node = BinaryOperatorNode(ValueNode(3.0), plus!!.implementation, ValueNode(2.0))
        assertEquals(ast.eval(), 5.0)
    }
}
