package stone.parser

import stone.Lexer
import stone.Token
import stone.ast.BinaryExpr
import stone.ast.BlockStmnt
import stone.ast.IfStmnt
import stone.ast.Name
import stone.ast.NegativeExpr
import stone.ast.NullStmnt
import stone.ast.NumberLiteral
import stone.ast.PrimaryExpr
import stone.ast.StringLiteral
import stone.ast.WhileStmnt
import java.io.FileReader
import java.util.HashSet

operator fun String.minus(parser: Parser): Parser = Parser().sep(this).ast(parser)
operator fun Parser.minus(parser: Parser): Parser = this.ast(parser)
operator fun Parser.minus(str: String): Parser = this.sep(str)
operator fun Parser.minus(set: Set<String>): Parser = this.sep(*set.toTypedArray())

fun Parser.or(func: Or.() -> Unit): Parser {
    val or = Or()
    or.func()
    return this.or(*or.list.toTypedArray())
}

class Or {
    val list: MutableList<Parser> = mutableListOf()

    fun or(f: () -> Parser) {
        list.add(f())
    }
}

fun main(args: Array<String>) {


    val reserved = HashSet<String>()
    val operators = Operators()

    reserved.add(";")
    reserved.add("}")
    reserved.add(Token.EOL)

    operators.add("=", 1, Operators.RIGHT)
    operators.add("==", 2, Operators.LEFT)
    operators.add(">", 2, Operators.LEFT)
    operators.add("<", 2, Operators.LEFT)
    operators.add("+", 3, Operators.LEFT)
    operators.add("-", 3, Operators.LEFT)
    operators.add("*", 4, Operators.LEFT)
    operators.add("/", 4, Operators.LEFT)
    operators.add("%", 4, Operators.LEFT)

    val NUMBER = Parser().number(NumberLiteral::class.java)
    val IDENTIFIER = Parser().identifier(Name::class.java, reserved)
    val STRING = Parser().string(StringLiteral::class.java)

    val END = setOf(";", Token.EOL)
    val END_WITH_STATEMENT = Parser().sep(";", Token.EOL)

    val NEGATIVE = Parser(NegativeExpr::class.java)

    val EXPR = Parser()
    val PRIMARY = Parser(PrimaryExpr::class.java)
    val PRIMARY_EMPTY = Parser(PrimaryExpr::class.java)
    val FACTOR = Parser()
    val STATEMENT = Parser()
    val BLOCK = Parser(BlockStmnt::class.java)
    val PROGRAM = Parser()

    val IFSTMNT = Parser(IfStmnt::class.java)
    val WHILESTMNT = Parser(WhileStmnt::class.java)
    val NULLSTMNT = Parser(NullStmnt::class.java)


    val primary = PRIMARY.or {
        or { "(" - EXPR - ")" }
        or { NUMBER }
        or { IDENTIFIER }
        or { STRING }
    }
    val factor = FACTOR.or {
        or { NEGATIVE - "-" - primary }
        or { primary }
    }
    val expr = EXPR.expression(BinaryExpr::class.java, factor, operators)

    val block = (BLOCK - "{")
            .option(STATEMENT)
            .repeat(
                    END_WITH_STATEMENT.option(STATEMENT)
            ) - "}"

    val simple = PRIMARY_EMPTY - expr

    val statement = STATEMENT.or {
        or {
            (IFSTMNT - "if" - expr - block).option("else" - block)
        }
        or { WHILESTMNT - "while" - expr - block }
        or { simple }
    }
    val program = PROGRAM.or {
        or { statement }
        or { NULLSTMNT }
    } - END

    val l = Lexer(FileReader("C:\\Users\\Gogo\\Desktop\\a.txt"))
    while (l.peek() !== Token.EOF) {
        val ast = program.parse(l)
        println("-> " + ast.toString())
    }
}
