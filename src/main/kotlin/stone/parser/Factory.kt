package stone.parser

import stone.Token
import stone.ast.ASTree

internal class ListFactory(type: Class<out ASTree>) {

    private val constructor = type.getConstructor(List::class.java)

    internal fun make(list: List<ASTree>): ASTree {
        if (list.size == 1) {
            return list.first()
        }
        return constructor.newInstance(list)
    }

}

internal class TokenFactory(type: Class<out ASTree>) {

    private val constructor = type.getConstructor(Token::class.java)

    internal fun make(token: Token): ASTree = constructor.newInstance(token)

}

