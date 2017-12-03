package stone

import stone.Parser.Companion.rule
import stone.Parser.Operators
import stone.ast.ASTree
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
import java.util.HashSet

class BasicParser {
    val reserved = HashSet<String>()
    val operators = Operators()

    val exprO = rule()
    val statementO = rule()


    val primary = rule(PrimaryExpr::class.java).or(
            rule().sep("(").ast(exprO).sep(")"),
            rule().number(NumberLiteral::class.java),
            rule().identifier(Name::class.java, reserved),
            rule().string(StringLiteral::class.java)
    )
    val factor = rule().or(
            rule(NegativeExpr::class.java).sep("-").ast(primary),
            primary
    )
    val expr = exprO.expression(BinaryExpr::class.java, factor, operators)

    val block = rule(BlockStmnt::class.java)
            .sep("{")
            .option(statementO)
            .repeat(
                    rule().sep(";", Token.EOL).option(statementO)
            )
            .sep("}")

    val simple = rule(PrimaryExpr::class.java).ast(expr)

    val statement = statementO.or(
            rule(IfStmnt::class.java)
                    .sep("if")
                    .ast(expr)
                    .ast(block)
                    .option(
                            rule().sep("else").ast(block)
                    ),
            rule(WhileStmnt::class.java)
                    .sep("while")
                    .ast(expr)
                    .ast(block),
            simple
    )
    val program = rule().or(
            statement,
            rule(NullStmnt::class.java)
    ).sep(";", Token.EOL)

    init {
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
    }

    @Throws(ParseException::class)
    fun parse(lexer: Lexer): ASTree {
        return program.parse(lexer)
    }

}
