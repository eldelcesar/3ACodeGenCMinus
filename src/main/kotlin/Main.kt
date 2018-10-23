import me.sargunvohra.lib.cakeparse.api.parseToEnd

fun main(args: Array<String>) {

    with(Parser) {
        try {
            // println("Coming Soon...")
            val input = "7 * 8 * 9 / 10"
            val goal = getParser()
            val result = LexAnalyzer.lexx.lex(input).parseToEnd(goal).value
            println("Result:\n$result")
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        }
    }
}