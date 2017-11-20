package stone.ast

import stone.Token
import java.lang.UnsupportedOperationException

abstract class ASLeaf : ASTree() {

    abstract val token: Token

    override val location: String
        get() = "line: ${token.lineNumber}"

    override val size: Int = 0

    override fun get(i: Int) = throw UnsupportedOperationException()

    override fun children(): Iterator<ASTree> = emptySequence<ASTree>().iterator()


}