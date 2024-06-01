package dev.zenhao.melon.command

import dev.zenhao.melon.command.argument.Argument
import dev.zenhao.melon.command.argument.impl.*

interface CommandBuilder {
    val index: Int

    fun <T : Argument<*>> addArgument(argument: T, block: CommandBuilder.(T) -> Unit)
}

fun <T : CommandBuilder> T.literal(block: CommandBuilder.() -> Unit) {
    this.block()
}

fun <T : CommandBuilder> T.key(block: CommandBuilder.(KeyArgument) -> Unit) {
    addArgument(KeyArgument(index + 1), block)
}

fun <T : CommandBuilder> T.friend(block: CommandBuilder.(FriendArgument) -> Unit) {
    addArgument(FriendArgument(index + 1), block)
}

fun <T : CommandBuilder> T.any(block: CommandBuilder.(AnyArgument) -> Unit) {
    addArgument(AnyArgument(index + 1), block)
}

fun <T : CommandBuilder> T.module(block: CommandBuilder.(ModuleArgument) -> Unit) {
    addArgument(ModuleArgument(index + 1), block)
}

fun <T : CommandBuilder> T.match(
    string: String,
    alias: Array<String> = emptyArray(),
    ignoreCase: Boolean = false,
    block: CommandBuilder.() -> Unit
) {
    addArgument(StringArgument(index + 1, string, alias, ignoreCase)) { block() }
}

fun <T : CommandBuilder> T.player(block: CommandBuilder.(PlayerArgument) -> Unit) {
    addArgument(PlayerArgument(index + 1), block)
}

fun <T : CommandBuilder> T.executor(description: String = "Empty", block: CommandExecutor.() -> Unit) {
    val executorArgument = ExecutorArgument(index + 1, description, block)
    addArgument(executorArgument) {}
}