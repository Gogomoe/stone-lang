package stone.ast

class BinaryExpr(list: List<ASTree>) : ASTList(list) {

    val left = list[0]
    val right = list[2]

    val operator = (list[1] as ASTLeaf).token.text

    override fun toString(): String = "<$left $operator $right>"

}