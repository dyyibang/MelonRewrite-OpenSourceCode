package team.exception.melon.util.extension

fun IntRange.randomRange(size: Int): IntRange {
    val newRange = this.first..this.last - size
    val start = newRange.random()
    val end = start + size

    return start..end
}