package stone.ast

import stone.Token

class StringLiteral(t: Token) : ASTLeaf(t) {

    fun value(): String = token.text

}