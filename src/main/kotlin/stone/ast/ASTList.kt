package stone.ast

abstract class ASTList(private val list: List<ASTree>) : ASTree() {

    override val size: Int = list.size

    override val location: String = list.first().location

    override fun get(i: Int) = list[i]

    override fun children(): Iterator<ASTree> = list.iterator()

    override fun toString(): String = list.toString()

}