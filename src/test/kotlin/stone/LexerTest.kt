package stone

import io.kotlintest.specs.StringSpec

class LexerTest : StringSpec() {

    init {
        val l = Lexer("""
        |while(i <= 10) {
        |   sum += i
        |   i++
        |}
        |""".trimMargin().reader())

        var token = l.read()
        while (token != Token.EOF) {
            println(token)
            token = l.read()
        }
    }

}