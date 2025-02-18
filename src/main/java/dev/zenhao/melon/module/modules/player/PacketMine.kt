package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.manager.HotbarManager.bypassTo
import dev.zenhao.melon.manager.HotbarManager.resetHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbarBypass
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.combat.ManualCev
import dev.zenhao.melon.module.modules.player.PacketMine.PacketType.Start
import dev.zenhao.melon.module.modules.player.PacketMine.PacketType.Stop
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.inventory.InventoryUtil.findBestItem
import melon.events.block.BlockEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.util.color.ColorRGB
import melon.system.util.color.ColorUtils.toRGB
import melon.utils.block.BlockUtil.calcBreakTime
import melon.utils.block.BlockUtil.canBreak
import melon.utils.entity.EntityUtils.eyePosition
import melon.utils.extension.minePacket
import melon.utils.extension.sendSequencedPacket
import melon.utils.graphics.ESPRenderer
import melon.utils.inventory.slot.allSlots
import melon.utils.inventory.slot.firstItem
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.world.getMiningSide
import net.minecraft.block.CobwebBlock
import net.minecraft.block.FireBlock
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import team.exception.melon.util.math.distanceSqToCenter
import team.exception.melon.util.math.scale

@Suppress("unused")
object PacketMine : Module(
    name = "PacketMine", langName = "发包挖掘", category = Category.PLAYER, description = "Better Mine."
) {
    private var debugMode = msetting("DebugMode", DebugType.Melon)
    private var mode = msetting("Mode", PacketMode.Instant)
    private var mode0 = mode.value as PacketMode
    private var maxRange by fsetting("MaxRange", 8.0f, 0.5f, 16f)
    private var safeSpamFactor by isetting("SafeSpamFactor", 350, 1, 1000).enumIs(mode, PacketMode.Spam)
    private var spamDelay by isetting("SpamDelay", 0, 0, 200).enumIs(mode, PacketMode.Spam)
    private var switchMode = msetting("SwitchMode", SwitchMode.Spoof)
    private var switchMode0 = switchMode.value as SwitchMode
    var inventoryTool by bsetting("InvTool", false).enumIs(switchMode, SwitchMode.Bypass)
    var doubleBreak by bsetting("DoubleBreak", false)
    private var setGround by bsetting("SetGround", true)
    private var backTime by isetting("BackTime", 0, 0, 500).isTrue { doubleBreak }
    private var rotate by bsetting("Rotate", true)
    private var prio by bsetting("PrioRotate", true).isTrue { rotate }
    private var swing by bsetting("Swing", false)
    private var mainColor by csetting("MainColor", ColorRGB(255, 32, 32))
    private var doubleColor by csetting("DoubleColor", ColorRGB(200, 32, 32)).isTrue { doubleBreak }
    private val renderer = ESPRenderer().apply { aFilled = 35; aOutline = 233 }
    private var packetTimer = TimerUtils()
    private var spamTimer = TimerUtils()
    private var retryTimer = TimerUtils()
    private var inventoryBypass = false
    private var packetSpamming = false
    private var fastSyncCheck = false
    private var forceRetry = false
    var blockData: BlockData? = null
    var doubleData: BlockData? = null

    override fun onDisable() {
        blockData = null
        doubleData = null
        fastSyncCheck = false
        packetSpamming = false
        timerReset()
        renderer.clear()
    }

    override fun onEnable() {
        blockData = null
        doubleData = null
        fastSyncCheck = false
        packetSpamming = false
        timerReset()
        renderer.clear()
    }

    override fun getHudInfo(): String {
        return mode0.name
    }

    init {
        safeEventListener<BlockEvent> { event ->
            BlockData(
                event.pos, event.facing, findBestItem(event.pos, inventoryBypass)?.let {
                    if (inventoryBypass) player.allSlots.firstItem(it)
                        ?.let { item -> HotbarSlot(item) } else player.hotbarSlots.firstItem(it)
                }, System.currentTimeMillis(), calcBreakTime(event.pos, inventoryBypass)
            ).apply {
                if (!canBreak(blockPos, false)) {
                    blockData = null
                    timerReset()
                    return@safeEventListener
                }
                if (doubleBreak) {
                    val blockData = blockData
                    val doubleData = doubleData
                    if (blockData != null && !world.isAir(blockData.blockPos) && doubleData == null) {
                        timerReset()
                        sendMinePacket(Stop, blockData)
                        PacketMine.doubleData = blockData
                    }
                }
                if (world.getBlockState(blockPos).block is CobwebBlock && findBestItem(
                        blockPos, inventoryBypass
                    ) !is SwordItem
                ) {
                    blockData = null
                    timerReset()
                    return@safeEventListener
                }
                if (blockData?.blockPos == event.pos) {
                    retryTimer.reset()
                    return@safeEventListener
                }
                blockData = this
                sendMinePacket(Start, this)
                timerReset()
                packetSpamming = true
            }
        }

        onLoop {
            blockData?.let { blockData ->
                if (!world.isAir(blockData.blockPos)) {
                    packetSpamming = true
                    packetTimer.reset()
                    forceRetry = retryTimer.passed(blockData.breakTime * 1.5)
                } else {
                    forceRetry = false
                    retryTimer.reset()
                }
                if (packetTimer.passed(safeSpamFactor) && world.isAir(blockData.blockPos)) {
                    packetSpamming = false
                }
                fastSyncCheck = if (!mode0.ignoreCheck) {
                    world.isAir(blockData.blockPos)
                } else {
                    packetTimer.passed(blockData.breakTime)
                }
            }
        }

        onMotion {
            mode0 = (mode.value as PacketMode)
            switchMode0 = (switchMode.value as SwitchMode)
            inventoryBypass = inventoryTool && switchMode0.bypass
            if (ManualCev.isEnabled) {
                if (ManualCev.stage == ManualCev.CevStage.Block || ManualCev.stage == ManualCev.CevStage.Place) return@onMotion
            }
            blockData?.let { blockData ->
                if (System.currentTimeMillis() - blockData.startTime < blockData.breakTime && debugMode.value == DebugType.DYZJCT) return@let
                if (player.distanceSqToCenter(blockData.blockPos) <= maxRange.sq) {
                    if (((mode0.ignoreCheck && packetSpamming) || !fastSyncCheck)) {
                        sendMinePacket(Stop, blockData)
                    }
                    if ((mode0.retry || forceRetry) && !player.isUsingItem) hookPos(blockData.blockPos, true)
                    if (mode0.strict) {
                        this@PacketMine.blockData = BlockData(
                            blockData.blockPos,
                            blockData.facing,
                            findBestItem(blockData.blockPos, inventoryBypass)?.let {
                                if (inventoryBypass) player.allSlots.firstItem(it)
                                    ?.let { item -> HotbarSlot(item) } else player.hotbarSlots.firstItem(it)
                            },
                            System.currentTimeMillis(),
                            calcBreakTime(blockData.blockPos, inventoryBypass)
                        )
                    }
                } else {
                    this@PacketMine.blockData = null
                    return@let
                }
            }
            doubleData?.let { doubleData ->
                //if (debugMode.value == DebugType.DYZJCT) blockData?.let { if (System.currentTimeMillis() - it.startTime < it.breakTime) sendMinePacket(Stop, it) }
                if (player.distanceSqToCenter(doubleData.blockPos) <= maxRange.sq && if (debugMode.value == DebugType.Melon) !player.isUsingItem else true) {
                    sendMinePacket(Stop, doubleData, true)
                } else {
                    this@PacketMine.doubleData = null
                    return@let
                }
            }
        }

        onRender3D {
            blockData?.let { blockData ->
                renderer.add(
                    Box(blockData.blockPos).scale(
                        Easing.OUT_CUBIC.inc(
                            Easing.toDelta(
                                blockData.startTime, blockData.breakTime
                            )
                        ).toDouble()
                    ), if (world.isAir(blockData.blockPos)) ColorRGB(32, 255, 32) else mainColor.toRGB()
                )
                renderer.render(it.matrices, true)
            }
            doubleData?.let { data ->
                renderer.add(
                    Box(data.blockPos).scale(
                        Easing.OUT_CUBIC.inc(
                            Easing.toDelta(
                                data.startTime, data.breakTime
                            )
                        ).toDouble()
                    ), if (world.isAir(data.blockPos)) ColorRGB(32, 255, 32) else doubleColor.toRGB()
                )
                renderer.render(it.matrices, true)
            }
        }
    }

    fun SafeClientEvent.hookPos(blockPos: BlockPos, reset: Boolean = false) {
        if (reset) blockData = null
        world.getBlockState(blockPos).onBlockBreakStart(world, blockPos, player)
        val side = getMiningSide(blockPos) ?: run {
            val vector = player.eyePosition.subtract(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)
            Direction.getFacing(vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat())
        }
        BlockEvent(blockPos, side).post()
        timerReset()
    }

    private fun timerReset() {
        packetTimer.reset()
        retryTimer.reset()
        spamTimer.reset()
    }

    private fun SafeClientEvent.sendMinePacket(
        action: PacketType, blockData: BlockData, db: Boolean = false
    ) {
        if (world.getBlockState(blockData.blockPos).block is FireBlock) {
            when (blockData) {
                this@PacketMine.blockData -> this@PacketMine.blockData = null
                this@PacketMine.doubleData -> this@PacketMine.doubleData = null
            }
            return
        }
        val toolSlot = blockData.mineTool ?: return
        if (rotate) RotationManager.addRotations(blockData.blockPos, prio)
        if (db) {
            if (switchMode0 != SwitchMode.Bypass || !inventoryTool) {
                if ((System.currentTimeMillis() - blockData.startTime) >= (blockData.breakTime + 500 + backTime)) {
                    resetHotbar()
                    if (world.isAir(doubleData?.blockPos)) doubleData = null
                    return
                } else if ((System.currentTimeMillis() - blockData.startTime) >= (blockData.breakTime + 100) && !player.usingItem) {
                    spoofHotbar(toolSlot)
                    if (setGround) player.onGround = true
                }
            } else {
                if ((System.currentTimeMillis() - blockData.startTime) >= (blockData.breakTime + 500 + backTime)) {
                    bypassTo(toolSlot)
                    if (world.isAir(doubleData?.blockPos)) doubleData = null
                } else if ((System.currentTimeMillis() - blockData.startTime) >= (blockData.breakTime + 100) && !player.usingItem) {
                    bypassTo(toolSlot)
                }
            }
        } else {
            if ((action == Stop && !spamTimer.passed(if (mode0.ignoreCheck) spamDelay else 0)) || player.isUsingItem) return
            if (swing) connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
            if (switchMode0.spoof) {
                if (!switchMode0.bypass) {
                    spoofHotbar(toolSlot) {
                        sendSequencedPacket(world) {
                            minePacket(action, blockData, it)
                        }
                    }
                } else {
                    spoofHotbarBypass(toolSlot) {
                        sendSequencedPacket(world) {
                            minePacket(action, blockData, it)
                        }
                    }
                }
            } else {
                if (switchMode0 != SwitchMode.Off) {
                    if (player.inventory.selectedSlot != blockData.mineTool.hotbarSlot) {
                        player.inventory.selectedSlot = blockData.mineTool.hotbarSlot
                    }
                    sendSequencedPacket(world) {
                        minePacket(action, blockData, it)
                    }
                }
            }
            spamTimer.reset()
        }
    }

    private enum class SwitchMode(val spoof: Boolean, val bypass: Boolean = false) {
        Spoof(true), Bypass(true, true), Swap(false), Off(false)
    }

    enum class PacketMode(val strict: Boolean, val retry: Boolean = false, val ignoreCheck: Boolean = false) {
        Instant(false), Spam(false, ignoreCheck = true), Packet(true), Legit(true, true)
    }

    enum class PacketType(val action: PlayerActionC2SPacket.Action) {
        Start(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK), Abort(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK), Stop(
            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
        )
    }

    enum class DebugType {
        Melon, DYZJCT
    }

    class BlockData(
        val blockPos: BlockPos,
        val facing: Direction,
        val mineTool: HotbarSlot?,
        val startTime: Long,
        val breakTime: Float
    )
}
