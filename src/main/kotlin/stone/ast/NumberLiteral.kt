package stone.ast

import stone.Token

class NumberLiteral(override val token: Token) : ASLeaf() {

    val value: Int = token.intValue

}