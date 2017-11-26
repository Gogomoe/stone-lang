package stone.ast

open class ASTList(private val children: List<ASTree>) : ASTree() {

    override val size: Int = children.size

    override val location: String
        get() = children.first().location

    override fun get(i: Int) = children[i]

    override fun children(): Iterator<ASTree> = children.iterator()

    override fun toString(): String = children.toString()

    fun child(i: Int): ASTree = children[i]

}