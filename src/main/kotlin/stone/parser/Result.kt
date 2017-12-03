package stone.parser

import stone.Lexer
import stone.ast.ASTree

internal data class Result(val lexer: Lexer, val list: MutableList<ASTree> = mutableListOf()) {
    fun peek() = lexer.peek()
    fun read() = lexer.read()

    fun add(element: ASTree) = list.add(element)
}