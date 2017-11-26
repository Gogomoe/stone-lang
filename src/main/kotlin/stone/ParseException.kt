package stone

class ParseException(override var message: String) : Exception() {

    constructor(token: Token) : this("", token)

    constructor(message: String, token: Token) : this("syntax error around ${location(token)} , $message")

    companion object {
        fun location(token: Token): String = if (token == Token.EOF) "the last line" else """"${token.text}" at line ${token.lineNumber}"""
    }
}