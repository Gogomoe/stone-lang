package stone.parser

import stone.Lexer
import stone.ast.ASTLeaf
import stone.ast.ASTList
import stone.ast.ASTree
import java.util.HashSet

class Parser(private val clazz: Class<out ASTree> = ASTList::class.java) {

    private val elements: MutableList<Element> = mutableListOf()

    private val factor = ListFactory(clazz)

    fun parse(lexer: Lexer): ASTree {
        val result = Result(lexer)
        elements.forEach { it.parse(result) }
        return factor.make(result.list)
    }

    fun match(lexer: Lexer): Boolean = if (elements.isEmpty()) true else elements.first().match(lexer)

    fun number(clazz: Class<out ASTLeaf>): Parser {
        elements.add(NumToken(clazz))
        return this
    }

    fun identifier(clazz: Class<out ASTLeaf>,
                   reserved: HashSet<String>): Parser {
        elements.add(IdToken(clazz, reserved))
        return this
    }

    fun string(clazz: Class<out ASTLeaf>): Parser {
        elements.add(StrToken(clazz))
        return this
    }

    fun sep(vararg pat: String): Parser {
        elements.add(Skip(setOf(*pat)))
        return this
    }

    fun ast(p: Parser): Parser {
        elements.add(Tree(p))
        return this
    }

    fun or(vararg p: Parser): Parser {
        elements.add(OrTree(listOf(*p)))
        return this
    }

    fun option(p: Parser): Parser {
        elements.add(Option(p))
        return this
    }

    fun repeat(p: Parser): Parser {
        elements.add(Repeat(p))
        return this
    }

    fun expression(clazz: Class<out ASTree>, subexp: Parser,
                   operators: Operators): Parser {
        elements.add(Expr(clazz, subexp, operators))
        return this
    }

    companion object {
        fun rule(clazz: Class<out ASTree>): Parser = Parser(clazz)
        fun rule(): Parser = Parser()
    }

}