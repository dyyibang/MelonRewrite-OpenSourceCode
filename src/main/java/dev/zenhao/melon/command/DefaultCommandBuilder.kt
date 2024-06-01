package dev.zenhao.melon.command

import dev.zenhao.melon.command.argument.Argument
import dev.zenhao.melon.command.argument.ArgumentTree

class DefaultCommandBuilder(
    override val index: Int,
    private val prevArgumentTree: ArgumentTree
) : CommandBuilder {
    override fun <T : Argument<*>> addArgument(argument: T, block: CommandBuilder.(T) -> Unit) {
        val argumentTree = ArgumentTree(argument)
        DefaultCommandBuilder(argument.index, argumentTree).block(argument)
        prevArgumentTree.appendArgument(argumentTree)
    }
}