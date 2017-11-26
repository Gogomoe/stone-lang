package stone.ast

import stone.Token

class NumberLiteral(override val token: Token) : ASTLeaf(token) {

    val value: Int = token.intValue

    override fun toString(): String = "<Number $value>"

}