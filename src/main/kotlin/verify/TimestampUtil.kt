package verify

import java.nio.ByteBuffer
import java.nio.ByteOrder

object TimestampUtil {
    fun currentTimeMillisArray(): ByteArray {
        val currentTimeMillis = System.currentTimeMillis()
        val byteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putLong(currentTimeMillis)

        return byteBuffer.array()
    }

    fun compare(timestampArray: ByteArray): Boolean {
        return timestampArray.contentEquals(currentTimeMillisArray())
    }
}