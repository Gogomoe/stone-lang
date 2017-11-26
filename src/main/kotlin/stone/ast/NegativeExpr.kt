package stone.ast

class NegativeExpr(c: List<ASTree>) : ASTList(c) {

    fun operand(): ASTree = child(0)

    override fun toString(): String = "-" + operand()

}