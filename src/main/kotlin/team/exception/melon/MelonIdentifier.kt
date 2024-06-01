package team.exception.melon

import dev.zenhao.melon.Melon
import net.minecraft.util.Identifier
import java.util.*

class MelonIdentifier(path: String) : Identifier(Melon.MOD_NAME.lowercase(Locale.ROOT), validatePath(path)) {
    companion object {
        fun validatePath(path: String): String {
            if (isValid(path)) return path

            val ret = StringBuilder()
            for (c in path.lowercase(Locale.getDefault()).toCharArray()) {
                if (isPathCharacterValid(c)) {
                    ret.append(c)
                }
            }
            return ret.toString()
        }
    }
}