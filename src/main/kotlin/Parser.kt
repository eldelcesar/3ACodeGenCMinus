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
import me.sargunvohra.lib.cakeparse.api.*
import me.sargunvohra.lib.cakeparse.lexer.Token
import me.sargunvohra.lib.cakeparse.lexer.TokenInstance
import me.sargunvohra.lib.cakeparse.parser.BaseParser

object Parser {

    // Recursive Rules
    val declarationListRef: BaseParser<TokenInstance> = ref { declarationList }
    val additiveExpressionRef: BaseParser<TokenInstance> = ref { additiveExpression }
    val paramListRef: BaseParser<TokenInstance> = ref { paramList }
    val localDeclarationsRef: BaseParser<TokenInstance?> = ref { localDeclarations }
    val statementListRef: BaseParser<TokenInstance?> = ref { statementList }
    val compoundStmtRef: BaseParser<TokenInstance> = ref { compoundStmt }
    val statementRef: BaseParser<TokenInstance> = ref { statement }
    val expressionRef: BaseParser<TokenInstance> = ref { expression }
    val termRef: BaseParser<TokenInstance> = ref { term }
    val variableRef: BaseParser<TokenInstance> = ref { variable }
    val argListRef: BaseParser<TokenInstance> = ref { argList }


    // Special characters Rules
    val relop = lessEqual or less or more or moreEqual or equal or notEqual

    val addop = plus or minus

    val mulop = times or divide

    val typeSpecifier = int_ or void_


    // General Rules
    val argList = (argListRef then comma then expressionRef) or
                                          expressionRef

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

    val expressionSmt = (expression then semicolon) or
                        semicolon

    val statement = expressionSmt or compoundStmtRef or selectionSmt or iterationSmt or returnSmt

    val statementList = (statementListRef then statement) or
                        empty<TokenInstance>()

    val varDeclaration = (typeSpecifier then id then semicolon) or
                                                 (typeSpecifier then id then leftBracket then number then rightParen then semicolon)

    val localDeclarations = (localDeclarationsRef then varDeclaration) or
                                                    empty<TokenInstance>()

    val compoundStmt = leftBrace then localDeclarations then statementList then rightBrace

    val param = (typeSpecifier then id) or
                                         (typeSpecifier then id then leftBracket then rightParen)

    val paramList = (paramListRef then comma then param) or
                                            param

    val params = paramList or void_

    val funDeclaration = typeSpecifier then id then leftParen then params then rightParen then compoundStmt

    val declaration = varDeclaration or funDeclaration

    val declarationList = ( declarationListRef then declaration) or
                                                    declaration

    val program = declarationList
}