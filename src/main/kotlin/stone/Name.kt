package stone

class Name(override val token: Token) : ASLeaf() {

    val value = token.stringValue

}