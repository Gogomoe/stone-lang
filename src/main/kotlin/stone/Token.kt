package stone

import java.lang.UnsupportedOperationException


sealed class Token(val lineNumber: Int) {

    open val intValue: Int
        get() = throw UnsupportedOperationException("不支持转换为数字")

    open val stringValue: String
        get() = throw UnsupportedOperationException("不支持字符串值")

    open val isIdentifier: Boolean = false

    open val isNumber: Boolean = false

    open val isString: Boolean = false


    companion object {
        val EOL = "\\n"
    }

    object EOF : Token(-1) {
        override val stringValue: String = "EOF"
    }

    override fun toString(): String = "<$stringValue>"

    internal class NumToken(lineNo: Int, id: Int) : Token(lineNo) {
        override val intValue: Int = id
        override val isNumber: Boolean = true
        override val stringValue: String = id.toString()
    }

    internal class IdToken(lineNo: Int, id: String) : Token(lineNo) {
        override val isIdentifier: Boolean = true
        override val stringValue: String = id
    }

    internal class StrToken(lineNo: Int, id: String) : Token(lineNo) {
        override val isString: Boolean = true
        override val stringValue: String = id
    }
}

