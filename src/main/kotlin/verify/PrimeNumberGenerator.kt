package verify

import team.exception.melon.util.extension.randomRange

object PrimeNumberGenerator {
    private fun isPrime(number: Int): Boolean {
        if (number <= 1) {
            return false
        }
        for (i in 2 until number) {
            if (number % i == 0) {
                return false
            }
        }
        return true
    }

    fun generatePrimes(range: IntRange): List<Int> {
        return range.filter { isPrime(it) }
    }

    fun generatePrime(range: IntRange): Int {
        return range.firstOrNull { isPrime(it) } ?: -1
    }

    tailrec fun generatePrimeFromRandomRange(originRange: IntRange, randomRangeSize: Int = 1000): Int {
        val primeNumber = generatePrime(originRange.randomRange(randomRangeSize))

        return if (primeNumber == -1) {
            generatePrimeFromRandomRange(originRange)
        } else {
            primeNumber
        }
    }
}