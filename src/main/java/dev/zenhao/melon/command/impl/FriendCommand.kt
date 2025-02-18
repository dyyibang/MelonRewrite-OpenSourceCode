package dev.zenhao.melon.command.impl

import dev.zenhao.melon.command.*
import dev.zenhao.melon.manager.FriendManager
import melon.utils.chat.ChatUtil

object FriendCommand : Command("friend", arrayOf("f"), "Friend commands") {
    init {
        literal {
            match("add") {
                any { anyArgument ->
                    executor("Add player to your friend list.") {
                        addFriend(anyArgument.value())
                    }
                }

                player {
                    executor("Add player to your friend list.") {
                        addFriend(it.value().name.string)
                    }
                }
            }

            match("del") {
                any { anyArgument ->
                    executor("Remove your friend from friend list.") {
                        removeFriend(anyArgument.value())
                    }
                }

                friend { friendArgument ->
                    executor("Remove your friend from friend list.") {
                        removeFriend(friendArgument.value())
                    }
                }
            }
        }
    }

    private fun addFriend(value: String) {
        FriendManager.addFriend(value)
        ChatUtil.sendMessage("Added friend $value.")
    }


    private fun removeFriend(value: String) {
        FriendManager.removeFriend(value)
        ChatUtil.sendMessage("Removed friend $value")
    }
}