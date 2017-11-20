package stone.ast

import stone.Token

class Name(override val token: Token) : ASLeaf() {

    val value = token.stringValue

}