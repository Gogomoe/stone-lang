package stone.parser

import stone.Lexer
import stone.ParseException
import stone.Token
import stone.ast.ASTLeaf
import stone.ast.ASTList

internal abstract class Element {

    abstract fun parse(result: Result)

    abstract fun match(lexer: Lexer): Boolean

}

internal class Tree(private val parser: Parser) : Element() {

    override fun parse(result: Result) {
        result.add(parser.parse(result.lexer))
    }

    override fun match(lexer: Lexer): Boolean = parser.match(lexer)

}

internal class OrTree(private val parsers: List<Parser>) : Element() {

    override fun parse(result: Result) {
        val parser = choose(result.lexer) ?: throw ParseException(result.peek())
        result.add(parser.parse(result.lexer))
    }

    override fun match(lexer: Lexer): Boolean = choose(lexer) != null

    private fun choose(lexer: Lexer): Parser? = parsers.find { it.match(lexer) }

}

internal class Option(private val parser: Parser) : Element() {

    override fun parse(result: Result) {
        if (parser.match(result.lexer)) {
            val t = parser.parse(result.lexer)
            if (t.javaClass != ASTList::class.java || t.size > 0)
                result.add(t)
        }
    }

    override fun match(lexer: Lexer): Boolean = parser.match(lexer)
}

internal class Repeat(parser: Parser) : Element() {

    private val option: Option = Option(parser)

    override fun parse(result: Result) {
        while (option.match(result.lexer)) {
            option.parse(result)
        }
    }

    override fun match(lexer: Lexer): Boolean = option.match(lexer)
}

internal abstract class AToken(private val type: Class<out ASTLeaf> = ASTLeaf::class.java) : Element() {

    private var factory: TokenFactory = TokenFactory(type)

    override fun parse(result: Result) {
        val t = result.read()
        if (test(t)) {
            result.add(factory.make(t))
        } else
            throw ParseException(t)
    }

    override fun match(lexer: Lexer): Boolean = test(lexer.peek())

    protected abstract fun test(t: Token): Boolean
}

internal class IdToken constructor(
        type: Class<out ASTLeaf>,
        private val reserved: Set<String> = emptySet()) : AToken(type) {

    override fun test(t: Token): Boolean {
        return t.isIdentifier && !reserved.contains(t.text)
    }
}

internal class NumToken constructor(type: Class<out ASTLeaf>) : AToken(type) {

    override fun test(t: Token): Boolean {
        return t.isNumber
    }
}

internal class StrToken constructor(type: Class<out ASTLeaf>) : AToken(type) {

    override fun test(t: Token): Boolean {
        return t.isString
    }
}

internal class Skip internal constructor(private var tokens: Set<String>) : Element() {

    override fun parse(result: Result) {
        val t = result.read()
        if (!t.match())
            throw ParseException(tokens.toString() + " expected.", t)
    }

    override fun match(lexer: Lexer): Boolean = lexer.peek().match()

    private fun Token.match() = this.isIdentifier && this.text in tokens
}
