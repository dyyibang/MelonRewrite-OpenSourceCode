import verify.PrimeNumberGenerator
import kotlin.system.measureTimeMillis

fun main() {
    val list = mutableListOf<Int>()

    val totalTime = measureTimeMillis {
        repeat(10) {
            val primeNumber = PrimeNumberGenerator.generatePrimeFromRandomRange(0..Int.MAX_VALUE, randomRangeSize = 5000)
            if (list.contains(primeNumber)) {
                println("Has same number: $primeNumber")
            } else {
                list.add(primeNumber)
            }
        }
    }

    println("Total time: $totalTime")
    list.forEach { println(it) }
}