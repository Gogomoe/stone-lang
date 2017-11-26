package stone.ast

import stone.Token

class Name(override val token: Token) : ASTLeaf(token) {

    val value = token.text

    override fun toString(): String = "<Name $value>"

}