package stone.ast

class PrimaryExpr(c: List<ASTree>) : ASTList(c) {
    companion object {
        fun create(c: List<ASTree>): ASTree {
            return if (c.size === 1) c[0] else PrimaryExpr(c)
        }
    }
}