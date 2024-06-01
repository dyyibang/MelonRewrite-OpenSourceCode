package dev.zenhao.melon.module.hud

import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.scaledHealth
import dev.zenhao.melon.utils.animations.MathUtils
import melon.system.event.SafeClientEvent
import melon.system.render.newfont.FontRenderers
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import team.exception.melon.util.math.distanceSqTo
import java.awt.Color

object PlayerListHUD :
    HUDModule(name = "PlayerListHUD", langName = "玩家列表", x = 170f, y = 170f, category = Category.HUD) {
    private val SafeClientEvent.playerList: List<PlayerEntity>
        get() = EntityManager.players.stream()
            .filter { it != player }
            .sorted(Comparator.comparingDouble { player.distanceSqTo(it.pos) })
            .toList()

    private val highlightList = arrayOf(
        "fin",
        "lemon",
        "yangchao",
        "nigger",
        "xbs1"
    )

    override fun onRender(context: DrawContext) {
        runSafe {
            val fontAdapter = FontRenderers.lexend
            val textHeight = fontAdapter.fontHeight
            var offsetY = 0f
            var maxTextWidth = 75f

            if (playerList.isEmpty()) {
                fontAdapter.drawString(
                    context.matrices,
                    "${Formatting.BOLD}${Formatting.GOLD}PlayerList Is Empty!",
                    x.toDouble(),
                    y.toDouble(),
                    Color.ORANGE.rgb
                )

                this@PlayerListHUD.height = textHeight
                this@PlayerListHUD.width =
                    fontAdapter.getStringWidth("${Formatting.BOLD}${Formatting.GOLD}PlayerList Is Empty!")

                return
            }

            playerList.forEach { p ->
                val playerName = if (highlightList.any { p.entityName.contains(it) }) {
                    "${Formatting.BOLD}${Formatting.BLACK}${p.entityName}"
                } else if (FriendManager.isFriend(p)) {
                    "${Formatting.AQUA}${p.entityName}"
                } else {
                    "${Formatting.RED}${p.entityName}"
                }

                val text =
                    "- ${Formatting.GREEN}${
                        MathUtils.round(
                            p.scaledHealth,
                            2
                        )
                    } $playerName ${Formatting.RESET}[${Formatting.BOLD}${Formatting.YELLOW}${
                        MathUtils.round(
                            p.distanceTo(player),
                            2
                        )
                    }${Formatting.RESET}]"

                fontAdapter.drawString(
                    context.matrices,
                    text,
                    PlayerListHUD.x.toDouble(),
                    (y + offsetY).toDouble(),
                    Color.WHITE.rgb
                )

                val playNameWidth = fontAdapter.getStringWidth(text)
                if (playNameWidth > maxTextWidth) {
                    maxTextWidth = playNameWidth
                }

                offsetY += textHeight + 2
            }

            this@PlayerListHUD.height = offsetY
            this@PlayerListHUD.width = maxTextWidth
        }
    }

//    override fun onRender(context: DrawContext) {
//        runSafe {
//            var offsetY = 0f
//            var maxFontWidth = 75f
//            if (playerList.isEmpty()) {
//                FontRenderers.cn.drawString(
//                    context.matrices,
//                    "${Formatting.BOLD}${Formatting.GOLD}PlayerList Is Empty!",
//                    x,
//                    y,
//                    Color.ORANGE.rgb
//                )
//                height = FontRenderers.cn.fontHeight
//                width = FontRenderers.cn.getStringWidth("${Formatting.BOLD}${Formatting.GOLD}PlayerList Is Empty!")
//                return
//            }
//            playerList.forEach { p ->
//                val playerName = if (highlightList.any { p.entityName.contains(it) }) {
//                    "${Formatting.BOLD}${Formatting.BLACK}${p.entityName}"
//                } else if (FriendManager.isFriend(p)) {
//                    "${Formatting.AQUA}${p.entityName}"
//                } else {
//                    "${Formatting.RED}${p.entityName}"
//                }
//
//                val text =
//                    "- ${Formatting.GREEN}${
//                        MathUtils.round(
//                            p.scaledHealth,
//                            2
//                        )
//                    } $playerName ${Formatting.RESET}[${Formatting.BOLD}${Formatting.YELLOW}${
//                        MathUtils.round(
//                            p.distanceTo(player),
//                            2
//                        )
//                    }${Formatting.RESET}]"
//
//                FontRenderers.cn.drawString(context.matrices, text, x, y + offsetY, Color.WHITE.rgb)
//                val playNameWidth = FontRenderers.cn.getStringWidth(text)
//                if (playNameWidth > maxFontWidth) {
//                    maxFontWidth = playNameWidth
//                }
//                offsetY += FontRenderers.cn.fontHeight + 2
//            }
//            height = offsetY
//            width = maxFontWidth
//        }
//    }
}