package dev.zenhao.melon.command.argument.impl

import dev.zenhao.melon.command.argument.Argument

class AnyArgument(index: Int) : Argument<String>(index) {
    override fun complete(input: String): List<String>? {
        return null
    }

    override fun convertToType(input: String): String? {
        return input.ifBlank {
            null
        }
    }

    override fun toString(): String {
        return "[Any]"
    }
}