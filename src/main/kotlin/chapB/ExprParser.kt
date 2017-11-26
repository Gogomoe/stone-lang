package chapB

import stone.CodeDialog
import stone.Lexer
import stone.ParseException
import stone.ast.ASTLeaf
import stone.ast.ASTree
import stone.ast.BinaryExpr
import stone.ast.NumberLiteral

import java.util.Arrays

class ExprParser(private val lexer: Lexer) {

    @Throws(ParseException::class)
    fun expression(): ASTree {
        var left = term()
        while (isToken("+") || isToken("-")) {
            val op = ASTLeaf(lexer.read())
            val right = term()
            left = BinaryExpr(Arrays.asList(left, op, right))
        }
        return left
    }

    @Throws(ParseException::class)
    fun term(): ASTree {
        var left = factor()
        while (isToken("*") || isToken("-")) {
            val op = ASTLeaf(lexer.read())
            val right = factor()
            left = BinaryExpr(Arrays.asList(left, op, right))
        }
        return left
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

        @Throws(ParseException::class)
        @JvmStatic

        fun main(args: Array<String>) {
            val lexer = Lexer(CodeDialog())
            val p = ExprParser(lexer)
            val t = p.expression()
            println("=> " + t)
        }
    }
}
