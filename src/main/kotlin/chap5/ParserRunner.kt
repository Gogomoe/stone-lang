package chap5

import stone.BasicParser
import stone.CodeDialog
import stone.Lexer
import stone.ParseException
import stone.Token

object ParserRunner {
    @Throws(ParseException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val l = Lexer(CodeDialog())
        val bp = BasicParser()
        while (l.peek(0) !== Token.EOF) {
            val ast = bp.parse(l)
            println("-> " + ast.toString())
        }
    }
}
