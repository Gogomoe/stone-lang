package stone

import stone.Token.IdToken
import stone.Token.NumToken
import stone.Token.StrToken
import java.io.LineNumberReader
import java.io.Reader
import java.util.regex.Matcher
import java.util.regex.Pattern

class Lexer(val r: Reader) {
    companion object {
        val regexPat: String = """\s*((//.*)|(\d+)|("(\\"|\\\\|\\n|[^"])*")|[A-Z_a-z]\w*|==|<=|>=|&&|\|\||\p{Punct})?"""
    }

    private val pattern: Pattern = Pattern.compile(regexPat)

    private val tokens: MutableList<Token> = mutableListOf()

    private var hasMore = true

    private val reader: LineNumberReader = LineNumberReader(r)

    fun read(): Token = if (fileQueue(0)) tokens.removeAt(0) else Token.EOF
    fun peek(i: Int): Token = if (fileQueue(i)) tokens[i] else Token.EOF

    fun peek() = peek(0)

    @Throws(ParseException::class)
    private fun fileQueue(i: Int): Boolean {
        while (i >= tokens.size) {
            if (hasMore) {
                readLine()
            } else {
                return false
            }
        }
        return true
    }

    @Throws(ParseException::class)
    private fun readLine() {
        val line = reader.readLine()
        if (line == null) {
            hasMore = false
            return
        }
        val lineNo = reader.lineNumber
        val matcher = pattern.matcher(line)
        matcher.useTransparentBounds(true).useAnchoringBounds(true)
        var pos = 0
        val end = line.length
        while (pos < end) {
            matcher.region(pos, end)
            if (matcher.lookingAt()) {
                addToken(lineNo, matcher)
                pos = matcher.end()
            } else {
                throw ParseException("bad token at line $lineNo")
            }
        }
        tokens.add(IdToken(lineNo, Token.EOL))
    }

    private fun addToken(lineNo: Int, matcher: Matcher) {
        val m = matcher.group(1)
        if (m != null) { // if not a space
            if (matcher.group(2) == null) { // if not a comment
                val token = when {
                    matcher.group(3) != null -> NumToken(lineNo, Integer.parseInt(m))
                    matcher.group(4) != null -> StrToken(lineNo, toStringLiteral(m))
                    else -> IdToken(lineNo, m)
                }
                tokens.add(token)
            }
        }
    }

    private fun toStringLiteral(s: String): String {
        val sb = StringBuilder()
        val len = s.length - 1
        var i = 0
        while (i < len) {
            var c = s[i]
            if (c == '\\' && i + 1 < len) {
                val c2 = s[i + 1]
                if (c2 == '"' || c2 == '\\') {
                    c = s[++i]
                } else if (c2 == 'n') {
                    ++i
                    c = '\n'
                }
            }
            sb.append(c)
        }
        return sb.toString()
    }

}

