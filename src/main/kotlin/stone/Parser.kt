package stone

import stone.ast.ASTLeaf
import stone.ast.ASTList
import stone.ast.ASTree
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class Parser {

    private var elements: MutableList<Element> = mutableListOf()
    private var factory: Factory = Factory.getForASTList(null)

    companion object {
        abstract class Element {
            @Throws(ParseException::class)
            internal abstract fun parse(lexer: Lexer, res: MutableList<ASTree>)

            @Throws(ParseException::class)
            internal abstract fun match(lexer: Lexer): Boolean
        }

        internal class Tree internal constructor(private var parser: Parser) : Element() {

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                res.add(parser.parse(lexer))
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                return parser.match(lexer)
            }
        }

        internal class OrTree internal constructor(private var parsers: Array<Parser>) : Element() {

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                val p = choose(lexer)
                if (p == null)
                    throw ParseException(lexer.peek(0))
                else
                    res.add(p.parse(lexer))
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                return choose(lexer) != null
            }

            @Throws(ParseException::class)
            private fun choose(lexer: Lexer): Parser? {
                return parsers.firstOrNull { it.match(lexer) }
            }

            internal fun insert(p: Parser) {
                val newParsers = mutableListOf<Parser>()
                newParsers.add(p)
                newParsers.addAll(parsers)
                parsers = newParsers.toTypedArray()
            }
        }

        internal class Repeat internal constructor(
                private var parser: Parser,
                private var onlyOnce: Boolean) : Element() {

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                while (parser.match(lexer)) {
                    val t = parser.parse(lexer)
                    if (t.javaClass != ASTList::class.java || t.size > 0) res.add(t)
                    if (onlyOnce) break
                }
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                return parser.match(lexer)
            }
        }

        internal abstract class AToken internal constructor(type: Class<out ASTLeaf>?) : Element() {
            private var factory: Factory? = null

            init {
                var type = type
                if (type == null)
                    type = ASTLeaf::class.java
                factory = Factory[type, Token::class.java]
            }

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                val t = lexer.read()
                if (test(t)) {
                    val leaf = factory!!.make(t)
                    res.add(leaf)
                } else
                    throw ParseException(t)
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                return test(lexer.peek(0))
            }

            internal abstract fun test(t: Token): Boolean
        }

        internal class IdToken internal constructor(type: Class<out ASTLeaf>, r: HashSet<String>?) : AToken(type) {
            private var reserved: HashSet<String> = r ?: HashSet()

            override fun test(t: Token): Boolean {
                return t.isIdentifier && !reserved.contains(t.text)
            }
        }

        internal class NumToken internal constructor(type: Class<out ASTLeaf>) : AToken(type) {

            override fun test(t: Token): Boolean {
                return t.isNumber
            }
        }

        internal class StrToken internal constructor(type: Class<out ASTLeaf>) : AToken(type) {

            override fun test(t: Token): Boolean {
                return t.isString
            }
        }

        open class Leaf internal constructor(private var tokens: Array<String>) : Element() {

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                val t = lexer.read()
                if (t.isIdentifier)
                    for (token in tokens)
                        if (token == t.text) {
                            find(res, t)
                            return
                        }
                if (tokens.isNotEmpty())
                    throw ParseException(tokens[0] + " expected.", t)
                else
                    throw ParseException(t)
            }

            open internal fun find(res: MutableList<ASTree>, t: Token) {
                res.add(ASTLeaf(t))
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                val t = lexer.peek(0)
                if (t.isIdentifier)
                    for (token in tokens)
                        if (token == t.text)
                            return true
                return false
            }
        }


        internal class Skip internal constructor(t: Array<String>) : Leaf(t) {

            override fun find(res: MutableList<ASTree>, t: Token) {}
        }

        class Precedence(internal var value: Int, internal var leftAssoc: Boolean // left associative
        )


        class Operators : HashMap<String, Precedence>() {

            fun add(name: String, prec: Int, leftAssoc: Boolean) {
                put(name, Precedence(prec, leftAssoc))
            }

            companion object {
                var LEFT = true
                var RIGHT = false
            }
        }

        internal class Expr internal constructor(
                clazz: Class<out ASTree>?,
                private var factor: Parser,
                private var ops: Operators) : Element() {
            private var factory: Factory = Factory.getForASTList(clazz)

            @Throws(ParseException::class)
            override fun parse(lexer: Lexer, res: MutableList<ASTree>) {
                var right = factor.parse(lexer)
                var prec: Precedence? = nextOperator(lexer)
                while (prec != null) {
                    right = doShift(lexer, right, prec.value)
                    prec = nextOperator(lexer)
                }
                res.add(right)
            }

            @Throws(ParseException::class)
            private fun doShift(lexer: Lexer, left: ASTree, prec: Int): ASTree {
                val list = ArrayList<ASTree>()
                list.add(left)
                list.add(ASTLeaf(lexer.read()))
                var right = factor.parse(lexer)
                var next: Precedence? = nextOperator(lexer)
                while (next != null && rightIsExpr(prec, next)) {
                    right = doShift(lexer, right, next.value)
                    next = nextOperator(lexer)
                }
                list.add(right)
                return factory.make(list)
            }

            @Throws(ParseException::class)
            private fun nextOperator(lexer: Lexer): Precedence? {
                val t = lexer.peek(0)
                return if (t.isIdentifier)
                    ops[t.text]
                else
                    null
            }

            private fun rightIsExpr(prec: Int, nextPrec: Precedence): Boolean {
                return if (nextPrec.leftAssoc)
                    prec < nextPrec.value
                else
                    prec <= nextPrec.value
            }

            @Throws(ParseException::class)
            override fun match(lexer: Lexer): Boolean {
                return factor.match(lexer)
            }
        }

        val factoryName = "create"

        private abstract class Factory {
            @Throws(Exception::class)
            internal abstract fun makeO(arg: Any): ASTree

            internal fun make(arg: Any): ASTree {
                try {
                    return makeO(arg)
                } catch (el: IllegalArgumentException) {
                    throw el
                } catch (e2: Exception) {
                    throw RuntimeException(e2) // this compiler is broken.
                }

            }

            companion object {

                internal fun getForASTList(clazz: Class<out ASTree>?): Factory {
                    var f = get(clazz, List::class.java)
                    if (f == null)
                        f = object : Factory() {
                            @Throws(Exception::class)
                            override fun makeO(arg: Any): ASTree {
                                val a: Any
                                val results = arg as List<ASTree>
                                return if (results.size == 1)
                                    results[0]
                                else
                                    ASTList(results)
                            }
                        }
                    return f
                }

                internal operator fun get(clazz: Class<out ASTree>?, argType: Class<*>): Factory? {
                    if (clazz == null)
                        return null
                    try {

                        val m = clazz.getMethod(factoryName, *arrayOf(argType))
                        return object : Factory() {
                            @Throws(Exception::class)
                            override fun makeO(arg: Any): ASTree {
                                return m.invoke(null, arg) as ASTree
                            }
                        }

                    } catch (e: NoSuchMethodException) {
                    }

                    try {
                        val c = clazz.getConstructor(argType)
                        return object : Factory() {
                            @Throws(Exception::class)
                            override fun makeO(arg: Any): ASTree {
                                return c.newInstance(arg)
                            }
                        }
                    } catch (e: NoSuchMethodException) {
                        throw RuntimeException(e)
                    }

                }
            }

        }

        fun rule(clazz: Class<out ASTree>): Parser {
            return Parser(clazz)
        }
    }


    constructor(clazz: Class<out ASTree>) {
        reset(clazz)
    }

    internal constructor(p: Parser) {
        elements = p.elements
        factory = p.factory
    }

    @Throws(ParseException::class)
    fun parse(lexer: Lexer): ASTree {
        val results = ArrayList<ASTree>()
        for (e in elements)
            e.parse(lexer, results)
        return factory.make(results)
    }

    @Throws(ParseException::class)
    internal fun match(lexer: Lexer): Boolean {
        return if (elements.size == 0)
            true
        else {
            val e = elements[0]
            e.match(lexer)
        }
    }

    fun reset(): Parser {
        elements = ArrayList()
        return this
    }

    fun reset(clazz: Class<out ASTree>?): Parser {
        elements = ArrayList()
        factory = Factory.getForASTList(clazz)
        return this
    }

    fun number(clazz: Class<out ASTLeaf>): Parser {
        elements.add(NumToken(clazz))
        return this
    }


    fun identifier(clazz: Class<out ASTLeaf>,
                   reserved: HashSet<String>): Parser {
        elements.add(IdToken(clazz, reserved))
        return this
    }

    fun string(clazz: Class<out ASTLeaf>): Parser {
        elements.add(StrToken(clazz))
        return this
    }

    fun token(vararg pat: String): Parser {
        val strs = mutableListOf<String>()
        strs.addAll(pat)
        elements.add(Leaf(strs.toTypedArray()))
        return this
    }

    fun sep(vararg pat: String): Parser {
        val strs = mutableListOf<String>()
        strs.addAll(pat)
        elements.add(Skip(strs.toTypedArray()))
        return this
    }

    fun ast(p: Parser): Parser {
        elements.add(Tree(p))
        return this
    }

    fun or(vararg p: Parser): Parser {
        val ps = mutableListOf<Parser>()
        ps.addAll(p)
        elements.add(OrTree(ps.toTypedArray()))
        return this
    }

    fun maybe(p: Parser): Parser {
        val p2 = Parser(p)
        p2.reset()
        elements.add(OrTree(arrayOf(p, p2)))
        return this
    }

    fun option(p: Parser): Parser {
        elements.add(Repeat(p, true))
        return this
    }

    fun repeat(p: Parser): Parser {
        elements.add(Repeat(p, false))
        return this
    }

    fun expression(subexp: Parser, operators: Operators): Parser {
        elements.add(Expr(null, subexp, operators))
        return this
    }

    fun expression(clazz: Class<out ASTree>, subexp: Parser,
                   operators: Operators): Parser {
        elements.add(Expr(clazz, subexp, operators))
        return this
    }

    fun insertChoice(p: Parser): Parser {
        val e = elements[0]
        if (e is OrTree)
            e.insert(p)
        else {
            val otherwise = Parser(this)
            reset(null)
            or(p, otherwise)
        }
        return this
    }

}

