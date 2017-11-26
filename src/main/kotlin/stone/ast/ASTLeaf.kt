package stone.ast

import stone.Token
import java.lang.UnsupportedOperationException

open class ASTLeaf(open val token: Token) : ASTree() {

    override val location: String
        get() = "line: ${token.lineNumber}"

    override val size: Int = 0

    override fun get(i: Int): ASTree {
        throw UnsupportedOperationException()
    }

    override fun children(): Iterator<ASTree> = emptySequence<ASTree>().iterator()

}