import me.sargunvohra.lib.cakeparse.api.parseToEnd
import java.io.File
import java.io.InputStream

fun main(args: Array<String>) {

    with(Parser) {
        try {
            val inputStream: InputStream = File("src/main/kotlin/input.cminus").inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            val goal = getParser()
            val result = LexAnalyzer.lexx.lex(inputString).parseToEnd(goal).value
            println("Result:\n$result")
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
        }
    }
}