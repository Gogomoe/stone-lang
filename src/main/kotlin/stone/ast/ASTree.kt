package stone.ast


abstract class ASTree : Iterable<ASTree> {

    abstract val size: Int

    abstract val location: String

    abstract operator fun get(i: Int): ASTree

    abstract fun children(): Iterator<ASTree>

    override fun iterator(): Iterator<ASTree> = children()
}