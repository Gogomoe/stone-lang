package stone.parser

import stone.Lexer
import stone.ast.ASTLeaf
import stone.ast.ASTList
import stone.ast.ASTree
import java.util.HashMap

class Operators : HashMap<String, Precedence>() {

    fun add(name: String, prec: Int, leftAssoc: Boolean) {
        put(name, Precedence(prec, leftAssoc))
    }

    companion object {
        @JvmField
        var LEFT = true
        @JvmField
        var RIGHT = false
    }
}

class Precedence(internal var value: Int, internal var leftAssoc: Boolean)

internal class Expr(private val clazz: Class<out ASTree> = ASTList::class.java,
                    private val factor: Parser,
                    private val operators: Operators) : Element() {

    private val factory = ListFactory(clazz)

    override fun parse(result: Result) {
        result.add(ExprParser(result.lexer).expression())
    }

    override fun match(lexer: Lexer): Boolean = factor.match(lexer)

    private inner class ExprParser(private val lexer: Lexer) {

        private fun Parser.parse() = this.parse(lexer)

        fun expression(): ASTree {
            var right = factor.parse()
            var next: Precedence? = nextOperator()
            while (next != null) {
                right = doShift(right, next.value)
                next = nextOperator()
            }
            return right
        }

        private fun doShift(left: ASTree, prec: Int): ASTree {
            val op = ASTLeaf(lexer.read())
            var right = factor.parse()
            var next: Precedence? = nextOperator()
            while (next != null && rightIsExpr(prec, next)) {
                right = doShift(right, next.value)
                next = nextOperator()
            }

            return factory.make(listOf(left, op, right))
        }

        private fun nextOperator(): Precedence? {
            val t = lexer.peek()
            return if (t.isIdentifier)
                operators[t.text]
            else
                null
        }

        private fun rightIsExpr(prec: Int, nextPrec: Precedence): Boolean {
            return if (nextPrec.leftAssoc)
                prec < nextPrec.value
            else
                prec <= nextPrec.value
        }

    }


}

