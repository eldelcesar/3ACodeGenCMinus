import me.sargunvohra.lib.cakeparse.api.parseToEnd

fun main(args: Array<String>) {

    with(Parser) {
        try {
            println("Coming Soon...")
            /*val input = " ( 7 ) "
            val result = LexAnalyzer.lexx.lex(input).parseToEnd(parenExp).value
            println("Result: $result")*/
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        }
    }
}