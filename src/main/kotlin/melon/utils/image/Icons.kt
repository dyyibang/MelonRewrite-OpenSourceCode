package melon.utils.image

import dev.zenhao.melon.Melon
import java.io.IOException
import java.io.InputStream

object Icons {
    @Throws(IOException::class)
    fun getIcons(): List<InputStream?> {
        val inputstream = Melon::class.java.getResourceAsStream("/assets/melon/logo/logo.png")
        return listOf(inputstream)
    }
}