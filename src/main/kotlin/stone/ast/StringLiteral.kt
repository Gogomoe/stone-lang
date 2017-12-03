package stone.ast

import stone.Token

class StringLiteral(t: Token) : ASTLeaf(t) {

    val value: String = token.text

    override fun toString(): String = "<String $value>"

}