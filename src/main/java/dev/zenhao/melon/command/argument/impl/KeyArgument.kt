package dev.zenhao.melon.command.argument.impl

import dev.zenhao.melon.command.argument.Argument
import dev.zenhao.melon.utils.KeyUtil

class KeyArgument(index: Int) : Argument<Int>(index) {
    override fun complete(input: String): List<String> {
        return listOf("[Key]")
    }

    override fun convertToType(input: String): Int {
        val keyCode = KeyUtil.parseToKeyCode(input.uppercase())

        if (keyCode == -1) {
            throw Exception("Unknown key name")
        }

        return keyCode
    }

    override fun toString(): String {
        return "[Key]"
    }
}