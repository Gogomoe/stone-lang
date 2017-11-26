package stone.ast

class IfStmnt(c: List<ASTree>) : ASTList(c) {

    fun condition(): ASTree = child(0)

    fun thenBlock(): ASTree = child(1)

    fun elseBlock(): ASTree? = if (size > 2) child(2) else null

    override fun toString(): String = "(if " + condition() + " " + thenBlock() + " else " + elseBlock() + ")"

}
