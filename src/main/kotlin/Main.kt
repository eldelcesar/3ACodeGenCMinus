import me.sargunvohra.lib.cakeparse.api.parseToEnd

fun main(args: Array<String>) {

    with(Parser) {
        try {
            val input = "x = (9 + 8 - 1 / 4)"
            val goal = getParser()
            val result = LexAnalyzer.lexx.lex(input).parseToEnd(goal).value
            println("Result:\n$result")
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        }
    }
}