import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.Lexer

object LexAnalyzer {
    // General use
    val number = token("number", "[0-9]+")
    val id = token("id","([A-Za-z]([A-Za-z0-9])*)|(_([_A-Za-z0-9])*[A-za-z]([A-Za-z0-9])*)")
    val space = token("space", "[ \\t\\r\\n]+",true)
    val semicolon = token("semicolon", ";")
    val comma = token("comma",",")
    val leftParen = token("lParen", "\\(")
    val rightParen = token("rParen", "\\)")
    val leftBracket = token("rParen", "\\[")
    val rightBracket = token("rParen", "\\]")
    val leftBrace = token("rParen", "\\{")
    val rightBrace = token("rParen", "\\}")
    // TODO: Regex to ignore comments (not mandatory, but will be nice though)

    // Arithmetic ops
    val plus = token("plus", "\\+")
    val minus = token("minus", "-")
    val times = token("times", "\\*")
    val divide = token("divide", "\\/")

    // Relational ops
    val more = token("divide", ">")
    val less = token("divide", "<")
    val moreEqual = token("divide", ">=")
    val lessEqual = token("divide", "<=")
    val equal = token("divide", "==")
    val notEqual = token("divide", "!=")

    // Assign
    val assign = token("assign", "=")

    // Special words
    val while_ = token("while", "while")
    val if_ = token("if", "if")
    val else_ = token("else", "else")
    val return_ = token("return", "return")

    // Type words
    val int_ = token("int", "int")
    val void_ = token("void", "void")

    val lexx: Lexer
        get() = setOf(while_, if_, else_, return_, int_, void_, number, space, comma, leftParen, rightParen,
                leftBracket, rightBracket, leftBrace, rightBrace, plus, minus, times, divide, more, less, moreEqual,
                lessEqual, equal, notEqual, assign, id).lexer()
}