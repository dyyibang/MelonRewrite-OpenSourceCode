package dev.zenhao.melon.command

import dev.zenhao.melon.command.argument.Argument
import dev.zenhao.melon.command.argument.ArgumentTree
import dev.zenhao.melon.command.argument.impl.StringArgument
import melon.system.util.interfaces.Alias

abstract class Command(
    final override val name: String,
    final override val alias: Array<out String> = emptyArray(),
    val description: String = "Empty"
) : Alias, CommandBuilder {
    override val index: Int = 0
    private val rootArgumentTree = ArgumentTree(StringArgument(0, name, alias, true))

    fun complete(args: List<String>): List<String> {
        return rootArgumentTree.complete(args)
    }

    fun invoke(input: String) {
        if (input.isEmpty()) {
            return
        }

        rootArgumentTree.invoke(input)
    }

    fun getArgumentTreeString(): String {
        return rootArgumentTree.getArgumentTreeString()
    }

    override fun <T : Argument<*>> addArgument(argument: T, block: CommandBuilder.(T) -> Unit) {
        val argumentTree = ArgumentTree(argument)
        DefaultCommandBuilder(argument.index, argumentTree).block(argument)
        rootArgumentTree.appendArgument(argumentTree)
    }
}