package chap5

import stone.BasicParser
import stone.Lexer
import stone.ParseException
import stone.Token
import java.io.FileReader

object ParserRunner {
    @Throws(ParseException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val l = Lexer(FileReader("C:\\Users\\Gogo\\Desktop\\a.txt"))
        val bp = BasicParser()
        while (l.peek(0) !== Token.EOF) {
            val ast = bp.parse(l)
            println("-> " + ast.toString())
        }
    }
}
