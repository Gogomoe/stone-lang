package stone.ast

class WhileStmnt(c: List<ASTree>) : ASTList(c) {

    fun condition(): ASTree = child(0)

    fun body(): ASTree = child(1)

    override fun toString(): String = "(while " + condition() + " " + body() + ")"

}
