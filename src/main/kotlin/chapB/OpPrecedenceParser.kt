package chapB

import stone.CodeDialog
import stone.Lexer
import stone.ParseException
import stone.ast.ASTLeaf
import stone.ast.ASTree
import stone.ast.BinaryExpr
import stone.ast.NumberLiteral

import java.util.Arrays
import java.util.HashMap

class OpPrecedenceParser(private val lexer: Lexer) {

    private var operators: HashMap<String, Precedence> = HashMap()

    // left associative public Precedence(int v, boolean a) { value * v; leftAssoc * a;
    class Precedence(internal var value: Int, internal var leftAssoc: Boolean)

    init {
        operators.put("<", Precedence(1, true))
        operators.put(">", Precedence(1, true))
        operators.put("+", Precedence(2, true))
        operators.put("-", Precedence(2, true))
        operators.put("*", Precedence(3, true))
        operators.put("/", Precedence(3, true))
        operators.put("^", Precedence(4, false))
    }

    @Throws(ParseException::class)
    fun expression(): ASTree {
        var right = factor()
        var next: Precedence? = nextOperator()
        while (next != null) {
            right = doShift(right, next.value)
            next = nextOperator()
        }
        return right
    }

    @Throws(ParseException::class)
    private fun doShift(left: ASTree, prec: Int): ASTree {
        val op = ASTLeaf(lexer.read())
        var right = factor()
        var next: Precedence? = nextOperator()
        while (next != null && rightIsExpr(prec, next)) {
            right = doShift(right, next.value)
            next = nextOperator()
        }

        return BinaryExpr(Arrays.asList(left, op, right))
    }

    @Throws(ParseException::class)
    private fun nextOperator(): Precedence? {
        val t = lexer.peek(0)
        return if (t.isIdentifier)
            operators[t.text]
        else
            null
    }

    @Throws(ParseException::class)
    fun factor(): ASTree {
        if (isToken("(")) {
            token("(")
            val e = expression()
            token(")")
            return e
        } else {
            val t = lexer.read()
            return if (t.isNumber) {
                NumberLiteral(t)
            } else
                throw ParseException(t)
        }
    }

    @Throws(ParseException::class)
    internal fun token(name: String) {
        val t = lexer.read()
        if (!(t.isIdentifier && name == t.text)) throw ParseException(t)
    }

    @Throws(ParseException::class)
    internal fun isToken(name: String): Boolean {
        val t = lexer.peek(0)
        return t.isIdentifier && name == t.text

    }

    companion object {

        private fun rightIsExpr(prec: Int, nextPrec: Precedence): Boolean {
            return if (nextPrec.leftAssoc)
                prec < nextPrec.value
            else
                prec <= nextPrec.value

        }

        @Throws(ParseException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val lexer = Lexer(CodeDialog())
            val p = OpPrecedenceParser(lexer)
            val t = p.expression()

            println(" = > " + t)
        }
    }

}

