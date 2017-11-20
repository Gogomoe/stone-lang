package stone

class NumberLiteral(override val token: Token) : ASLeaf() {

    val value: Int = token.intValue

}