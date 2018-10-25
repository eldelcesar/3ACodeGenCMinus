import LexAnalyzer.assign
import LexAnalyzer.comma
import LexAnalyzer.divide
import LexAnalyzer.else_
import LexAnalyzer.equal
import LexAnalyzer.id
import LexAnalyzer.if_
import LexAnalyzer.int_
import LexAnalyzer.leftBrace
import LexAnalyzer.leftBracket
import LexAnalyzer.leftParen
import LexAnalyzer.less
import LexAnalyzer.lessEqual
import LexAnalyzer.minus
import LexAnalyzer.more
import LexAnalyzer.moreEqual
import LexAnalyzer.notEqual
import LexAnalyzer.number
import LexAnalyzer.plus
import LexAnalyzer.return_
import LexAnalyzer.rightBrace
import LexAnalyzer.rightBracket
import LexAnalyzer.rightParen
import LexAnalyzer.semicolon
import LexAnalyzer.times
import LexAnalyzer.void_
import LexAnalyzer.while_
import com.sun.xml.internal.rngom.parse.host.Base
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.Token
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import me.sargunvohra.lib.cakeparse.parser.BaseParser
import java.util.*
import kotlin.math.exp
import kotlin.math.min

object Parser {

    var countTemp = 0
    var countLabel = 0
    val tempStack: Stack<String> = Stack<String>()
    val tempLabel: Stack<String> = Stack<String>()

    // Recursive References
    val termRef: BaseParser<String?> = ref { term }
    val additiveExpressionRef: BaseParser<String?> = ref { additiveExpression }
    val expressionRef: BaseParser<String?> = ref { expression }
    val argListRef: BaseParser<String?> = ref { argList }
    val statementListRef: BaseParser<Any?> = ref { statementList }
    val compoundStmtRef: BaseParser<Any?> = ref { compoundStmt }
    val localDeclarationsRef: BaseParser<Any?> = ref { localDeclarations }
    val paramListRef: BaseParser<Any?> = ref { paramList }
    val declarationListRef: BaseParser<Any?> = ref { declarationList }
    val declarationRef: BaseParser<Any?> = ref { declaration }
    val funDeclarationRef: BaseParser<Any?> = ref { funDeclaration }
    val varDeclarationRef: BaseParser<Any?> = ref { varDeclaration }
    val statementRef: BaseParser<Any?> = ref { statement }
    val expressionStmtRef: BaseParser<Any?> = ref { expressionStmt }
    val selectionSmtRef: BaseParser<Any?> = ref { selectionSmt }
    val iterationSmtRef: BaseParser<Any?> = ref { iterationSmt }
    val typeSpecifierRef: BaseParser<Any?> = ref { typeSpecifier }
    val callRef: BaseParser<String> = ref { call }
    val paramsRef: BaseParser<Any?> = ref { params }
    val paramRef: BaseParser<Any?> = ref { param }
    val variableRef: BaseParser<String> = ref { variable }
    val simpleExpressionRef: BaseParser<String?> = ref { simpleExpression }
    val factorRef: BaseParser<String> = ref { factor }
    val argsRef: BaseParser<String?> = ref { args }


    // Operators
    val relop = lessEqual or less or more or moreEqual or equal or notEqual
    val mulop = times or divide
    val addop = plus or minus
    val typeSpecifier = int_ or void_


    // Rules
    val argList: BaseParser<String>  =  expressionRef map {
        exp -> val a = exp
        a?.let { it } ?: throw IllegalStateException()
    } or ((argListRef before comma and expressionRef) map {
        exp -> val(a,b) = exp
        a?.let {
            b?.let {
                "$a\n$b"
            }
        } ?: throw IllegalStateException()
    })

    val args = argListRef

    val call = id and (leftParen then argsRef before rightParen) map {
        exp -> val (a, b) = exp
        new_Temp()
        val temp = tempStack.pop()
        new_Temp()
        val temp2 = tempStack.pop()
        "begin_args\n" +
                "$temp = $b\n" +
                "param $temp\n" +
                "$temp2 = call ${a.raw}, 1"
    }

    val variable: BaseParser<String> = (id and (leftBracket then expressionRef before rightBracket) map {
        exp -> val (a, b) = exp
        new_Temp()
        new_Temp()
        new_Temp()
        b?.let {
            val temp1 = tempStack.pop()
            val temp2 = tempStack.pop()
            val temp3 = tempStack.pop()
            "\n$temp3 = sizeof ${a.raw}\n" +
                    "$temp2 = $b * $temp3\n" +
                    "$temp1 = &${a.raw} + $temp1"
        } ?: throw IllegalStateException()
    }) or (id map { it.raw })

    val factor: BaseParser<String> = (number map { it.raw }) or callRef or variableRef or ((leftParen then expressionRef before rightParen) map {
        exp -> val a = exp
        a?.let { it } ?: throw IllegalStateException()
    })

    val term: BaseParser<String> = ((factorRef and mulop) and termRef map {
        exp -> val (a, b) = exp
        new_Temp()
        when (a.second.type) {
            times -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} * $b"
            }
            divide -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} / $b"
            }
            else -> throw IllegalStateException()
        }
    }) or factor

    val additiveExpression = ((termRef and addop) and additiveExpressionRef map {
        exp -> val (a, b) = exp
        new_Temp()
        when(a.second.type) {
            plus -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} + $b"
            }
            minus -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} - $b"
            }
            else -> throw IllegalStateException()
        }
    }) or term

    val simpleExpression = (additiveExpressionRef and relop and additiveExpressionRef) map {
        exp -> val (a, b) = exp
        new_Temp()
        new_Temp()
        when(a.second.type) {
            lessEqual -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} <= $b"
            }
            moreEqual -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} >= $b"
            }
            less -> {
                val temp1 = tempStack.pop()
                val temp2 = tempStack.pop()
                "$temp1\n$temp1 = ${a.first}\n$temp1 = $temp2 < $b"
            }
            more -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} > $b"
            }
            equal -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} == $b"
            }
            notEqual -> {
                val temp = tempStack.pop()
                "$temp\n$temp = ${a.first} != $b"
            }
            else -> throw IllegalStateException()
        }
    } or additiveExpressionRef

    val expression: BaseParser<String?> = ((variableRef and assign map { it }) and expressionRef map {
        exp -> val (a, b) = exp
        "${a.first} ${a.second.raw} $b"
    }) or simpleExpressionRef

    val expressionStmt =  expressionRef before semicolon or semicolon

    val param = typeSpecifierRef then id

    val paramList =  (paramRef before comma and paramListRef map {""}) or paramRef map { "" }

    val params = paramListRef or void_

    val varDeclaration = typeSpecifierRef then id before semicolon map { "" }

    val localDeclarations = (varDeclarationRef and localDeclarationsRef) or varDeclarationRef

    val compoundStmt = leftBrace then localDeclarationsRef then statementListRef before rightBrace map {
        exp -> val a = exp
        a?.let {
            a.toString() }
    }

    val returnSmt = (return_ before semicolon map { it.raw }) or (return_ and expressionRef before semicolon map { "${it.first.raw} ${it.second}" })

    val funDeclaration = typeSpecifierRef then id and (leftParen then paramsRef before rightParen) map {
        exp -> val (a, b) = exp
        b?.let {
            "entry ${a.raw}\n$b"
        } ?: throw IllegalStateException()
    } and compoundStmt


    val iterationSmt = (while_ then leftParen then expressionRef before rightParen) and statementListRef map {
        exp -> val (a, b) = exp
        new_Temp()
        new_Label()
        a?.let {
            val temp = tempStack.pop()
            val label = tempLabel.pop()
            "$temp = $a\n" +
                    "if false $temp goto $label\n$b\n" +
                    "Label $label\n"
        } ?: throw IllegalStateException()
    }

    val selectionSmt = (((if_ then leftParen then expressionRef before rightParen) and statementListRef) and (else_ then statementListRef) map{
        exp -> val (a, b) = exp
        new_Temp()
        new_Label()
        a.first?.let {
            val temp = tempStack.pop()
            val label = tempLabel.pop()
            "$temp = ${a.first}\nif false $temp goto $label\n${a.second}\n" +
                    "Label $label\n$b"
        } ?: throw IllegalStateException()
    }) or ((if_ then leftParen then expressionRef before rightParen) and statementListRef map{
        exp -> val (a, b) = exp
        a?.let {
            "t1 = $a\n if false t1 goto L1\n$b"
        } ?: throw IllegalStateException()
    })

    val statement = (expressionStmtRef or compoundStmtRef or selectionSmtRef or iterationSmtRef or varDeclarationRef or returnSmt) map { it.toString() }

    val statementList = (statementRef and statementListRef map {
        exp -> val (a, b) = exp
        a?.let {
            b?.let { "$a\n$b " }
        } ?: throw IllegalStateException()
    }) or statementRef


    val declaration = funDeclarationRef or varDeclarationRef
    val declarationList = (declaration and declarationListRef) or declaration
    val program = declarationList

    // Support functions
    fun getParser(): BaseParser<Any?> { return program }

    fun new_Temp(): String {
        tempStack.push("T$countTemp")
        this.countTemp++
        return "T$countTemp"
    }

    fun new_Label(): String {
        tempLabel.push("L$countLabel")
        this.countLabel++
        return "L$countLabel"
    }






    // Recursive Rules
    /*val declarationListRef: BaseParser<TokenInstance> = ref { declarationList }
    val additiveExpressionRef: BaseParser<TokenInstance> = ref { additiveExpression }
    val paramListRef: BaseParser<TokenInstance> = ref { paramList }
    val localDeclarationsRef: BaseParser<TokenInstance?> = ref { localDeclarations }
    val statementListRef: BaseParser<TokenInstance?> = ref { statementList }
    val compoundStmtRef: BaseParser<TokenInstance> = ref { compoundStmt }
    val statementRef: BaseParser<TokenInstance> = ref { statement }
    val expressionRef: BaseParser<TokenInstance> = ref { expression }
    val termRef: BaseParser<TokenInstance> = ref { term }
    val variableRef: BaseParser<TokenInstance> = ref { variable }
    val argListRef: BaseParser<TokenInstance> = ref { argList }*/


    // Special characters Rules
    /*
    val relop = lessEqual or less or more or moreEqual or equal or notEqual
    val addop = plus or minus
    val mulop = times or divide
    val typeSpecifier = int_ or void_
    */


    // General Rules
    /*
    val argList = (argListRef then comma then expressionRef) or expressionRef
    val args = argList or empty<TokenInstance>()
    val call = id then leftParen then args then rightParen
    val factor = (leftParen then expressionRef then rightParen) or
                                         variableRef or call or number
    val term = (termRef then mulop then factor) or factor
    val additiveExpression = (additiveExpressionRef then addop then term) or
                             term
    val simpleExpression = (additiveExpression then relop then additiveExpression) or
                                                   (additiveExpression)
    val variable = id or (id then leftBracket then expressionRef then rightBracket)
    val expression = (variable then assign then expressionRef) or
                     (simpleExpression)
    val returnSmt = (return_ then semicolon) or
                                            (return_ then expression)
    val iterationSmt = while_ then leftParen then expression then statementRef
    val selectionSmt = (if_ then leftParen then expression then rightParen then statementRef) or
                       (if_ then leftParen then expression then rightParen then statementRef then else_ then statementRef)
    val expressionSmt = (expression then semicolon) or semicolon
    val statement = expressionSmt or compoundStmtRef or selectionSmt or iterationSmt or returnSmt
    val statementList = (statementListRef then statement) or empty<TokenInstance>()
    val varDeclaration = (typeSpecifier then id then semicolon) or
                                                 (typeSpecifier then id then leftBracket then number then rightParen then semicolon)
    val localDeclarations = (localDeclarationsRef then varDeclaration) or empty<TokenInstance>()
    val compoundStmt = leftBrace then localDeclarations map {} then statementList before rightBrace
    val param = (typeSpecifier then id) or
                                         (typeSpecifier then id then leftBracket then rightParen)
    val paramList = (paramListRef then comma then param) or
                                            param
    val params = paramList or void_
    val funDeclaration = typeSpecifier then id then leftParen then params then rightParen then compoundStmt
    val declaration = varDeclaration or funDeclaration
    val declarationList = ( declarationListRef then declaration) or declaration
    val program = declarationList
    */
}
