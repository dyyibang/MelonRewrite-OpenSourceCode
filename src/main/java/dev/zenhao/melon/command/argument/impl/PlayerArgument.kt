package dev.zenhao.melon.command.argument.impl

import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.command.argument.Argument
import net.minecraft.entity.player.PlayerEntity

class PlayerArgument(index: Int) : Argument<PlayerEntity>(index) {
    override fun complete(input: String): List<String> {
        return EntityManager.players
            .map { it.name.string }
            .filter { it.startsWith(input, true) }
    }

    override fun convertToType(input: String): PlayerEntity? {
        return EntityManager.players
            .firstOrNull { it.name.string.equals(input, true) }
    }

    override fun toString(): String {
        return "[Player]"
    }
}