package dev.zenhao.melon.module.modules.crystal

import dev.zenhao.melon.manager.*
import dev.zenhao.melon.manager.FriendManager.isFriend
import dev.zenhao.melon.manager.HotbarManager.serverSideItem
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbarBypass
import dev.zenhao.melon.manager.HotbarManager.spoofInvBypass
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.isResistant
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.calcCollidingCrystalDamage
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.canMove
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.checkBreakRange
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getCrystalSlot
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getMaxCrystalSlot
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.isReplaceable
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.realSpeed
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.scaledHealth
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.totalHealth
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.extension.ceilToInt
import dev.zenhao.melon.utils.extension.synchronized
import dev.zenhao.melon.utils.extension.toDegree
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.math.RotationUtils
import dev.zenhao.melon.utils.math.RotationUtils.normalizeAngle
import it.unimi.dsi.fastutil.ints.Int2LongMaps
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import melon.events.RunGameLoopEvent
import melon.events.TickEvent
import melon.events.WorldEvent
import melon.events.player.PlayerMotionEvent
import melon.events.render.Render3DEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeConcurrentListener
import melon.system.event.safeEventListener
import melon.system.event.safeParallelListener
import melon.system.render.graphic.Render2DEngine
import melon.system.render.graphic.Render3DEngine
import melon.system.util.color.ColorRGB
import melon.system.util.delegate.CachedValueN
import melon.system.util.interfaces.DisplayEnum
import melon.utils.block.BlockUtil.canBreak
import melon.utils.block.BlockUtil.canSee
import melon.utils.chat.ChatUtil
import melon.utils.combat.CrystalUtils
import melon.utils.combat.CrystalUtils.crystalPlaceBoxIntersectsCrystalBox
import melon.utils.combat.ExposureSample
import melon.utils.concurrent.threads.onMainThread
import melon.utils.concurrent.threads.runSafe
import melon.utils.concurrent.threads.runSynchronized
import melon.utils.entity.EntityUtils.eyePosition
import melon.utils.entity.EntityUtils.isntValid
import melon.utils.extension.fastPos
import melon.utils.extension.sendSequencedPacket
import melon.utils.graphics.ESPRenderer
import melon.utils.inventory.slot.*
import melon.utils.item.attackDamage
import melon.utils.item.duraPercentage
import melon.utils.math.TpsCalculator
import melon.utils.world.getMiningSide
import melon.utils.world.noCollision
import net.minecraft.block.Blocks
import net.minecraft.block.FireBlock
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.EnchantedGoldenAppleItem
import net.minecraft.item.Items
import net.minecraft.item.SwordItem
import net.minecraft.item.ToolItem
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import team.exception.melon.util.math.distanceSq
import team.exception.melon.util.math.distanceSqTo
import team.exception.melon.util.math.distanceSqToCenter
import team.exception.melon.util.math.toVec3dCenter
import team.exception.melon.util.math.vector.Vec2f
import java.awt.Color
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import kotlin.math.*

/**
 * Created by zenhao on 18/12/2022.
 * Updated by zenhao on 22/07/2023.
 */
object MelonAura2 : Module(
    name = "MelonAura2", langName = "自动水晶", category = Category.COMBAT, description = "Auto using crystals for pvp."
) {
    private var p = msetting("Page", Page.GENERAL)

    //Page GENERAL
    var damageMode = msetting("DamageMode", DamageMode.PPBP).enumIs(p, Page.GENERAL)
    private var switchMode0 = msetting("Switch", Switch.SpoofBypass).enumIs(p, Page.GENERAL)
    private var switchMode = (switchMode0.value as Switch)
    private var antiWeakness = msetting("AntiWeakness", AntiWeaknessMode.Spoof).enumIs(p, Page.GENERAL)
    private var swingMode = msetting("Swing", SwingMode.Off).enumIs(p, Page.GENERAL)
    private var strictDirection = bsetting("StrictDirection", false).enumIs(p, Page.GENERAL)
    private var rotate = bsetting("Rotate", false).enumIs(p, Page.GENERAL)
    private var yawSpeed = fsetting("YawSpeed", 30.0f, 5.0f, 180f, 1f).isTrue(rotate).enumIs(p, Page.GENERAL)
    private var rotateDiff = isetting("RotationDiff", 2, 0, 180).isTrue(rotate).enumIs(p, Page.GENERAL)
    private var eatingPause = bsetting("EatingPause", false).enumIs(p, Page.GENERAL)
    private var old = bsetting("OldPlace", false).enumIs(p, Page.GENERAL)
    private var wallRange = dsetting("WallRange", 3.0, 0.0, 6.0).isTrue(old).enumIs(p, Page.GENERAL)

    //Page Place
    private var packetPlaceMode = msetting("PacketMode", PacketPlaceMode.Strong).enumIs(p, Page.PLACE)
    private var packetPlace0 = (packetPlaceMode.value as PacketPlaceMode)
    private var placeSwing = bsetting("PlaceSwing", false).enumIs(p, Page.PLACE)
    private var placeDelay = isetting("PlaceDelay", 45, 1, 1000).enumIs(p, Page.PLACE)
    var placeRange = dsetting("PlaceRange", 5.5, 1.0, 6.0).enumIs(p, Page.PLACE)
    private var placeMinDmg = dsetting("PlaceMinDmg", 4.0, 0.0, 36.0).enumIs(p, Page.PLACE)
    private var placeMaxSelf = isetting("PlaceMaxSelfDmg", 10, 0, 36).enumIs(p, Page.PLACE)
    private var placeBalance = fsetting("PlaceBalance", -3f, -10f, 10f).enumIs(p, Page.PLACE)

    //Page Break
    private var explodeMode = msetting("ExplodeMode", ExplodeMode.Normal).enumIs(p, Page.BREAK)
    private var packetSync by bsetting(
        "PacketSync", false
    ).isTrue { explodeMode.value == ExplodeMode.Sync || explodeMode.value == ExplodeMode.Both }.enumIs(p, Page.BREAK)
    private var packetDelay by isetting(
        "PacketDelay", 35, 0, 50, 1
    ).isTrue { explodeMode.value == ExplodeMode.Sync || explodeMode.value == ExplodeMode.Both }.enumIs(p, Page.BREAK)
    private var hitDelay = isetting("HitDelay", 55, 0, 500, 1).enumIs(p, Page.BREAK)
    private var breakRange = fsetting("BreakRange", 5.5f, 1f, 6f).enumIs(p, Page.BREAK)
    private var breakMinDmg = dsetting("BreakMinDmg", 1.0, 0.0, 36.0).enumIs(p, Page.BREAK)
    private var breakMaxSelf = isetting("BreakMaxSelf", 12, 0, 36).enumIs(p, Page.BREAK)
    private val breakBalance = fsetting("BreakBalance", -7.0f, -10.0f, 10.0f).enumIs(p, Page.BREAK)

    //Page Calculation
    private var maxTargets = isetting("MaxTarget", 3, 1, 8).enumIs(p, Page.CALCULATION)
    private var motionPredict = bsetting("MotionPredict", true).enumIs(p, Page.CALCULATION)
    private var predictTicks = isetting("PredictTicks", 12, 1, 20).isTrue(motionPredict).enumIs(p, Page.CALCULATION)
    private var debug = bsetting("Debug", false).enumIs(p, Page.CALCULATION)
    private var enemyRange = isetting("EnemyRange", 8, 1, 10).enumIs(p, Page.CALCULATION)
    private var noSuicide = fsetting("NoSuicide", 2f, 0f, 20f).enumIs(p, Page.CALCULATION)
    var ownPredictTicks = isetting("OwnPredictTicks", 2, 0, 20).enumIs(p, Page.CALCULATION)

    //Page Force
    private var slowFP = bsetting("SlowFacePlace", true).enumIs(p, Page.FORCE)
    private var fpDelay = isetting("FacePlaceDelay", 350, 1, 750).isTrue(slowFP).enumIs(p, Page.FORCE)
    private val forcePlaceBalance = fsetting("ForcePlaceBalance", -1.5f, -10.0f, 10.0f).enumIs(p, Page.FORCE)
    private var forceHealth = isetting("ForceHealth", 6, 0, 20).enumIs(p, Page.FORCE)
    private var forcePlaceMotion = fsetting("ForcePlaceMotion", 5f, 0.25f, 10f).enumIs(p, Page.FORCE)
    var forcePlaceDmg = dsetting("ForcePlaceDamage", 0.5, 0.1, 10.0).enumIs(p, Page.FORCE)
    private var armorRate = isetting("ForceArmor%", 25, 0, 100).enumIs(p, Page.FORCE)
    private val armorDdos = bsetting("ArmorDdos", false).enumIs(p, Page.FORCE)
    private val ddosMinDamage = fsetting("DdosMinDamage", 1.5f, 0.0f, 10.0f).isTrue(armorDdos).enumIs(p, Page.FORCE)
    private val ddosQueueSize = isetting("DdosQueueSize", 5, 0, 10).isTrue(armorDdos).enumIs(p, Page.FORCE)
    private val ddosDamageStep = fsetting("DdosDamageStep", 0.1f, 0.1f, 5.0f).isTrue(armorDdos).enumIs(p, Page.FORCE)

    //Page Lethal
    private var antiSurround = bsetting("AntiSurround", false).enumIs(p, Page.LETHAL)
    private var blockBoost = bsetting("BlockBoost", false).enumIs(p, Page.LETHAL)

    //Page Render
    private var renderDamage = bsetting("RenderDamage", true).enumIs(p, Page.RENDER)
    private var motionRender = bsetting("MotionRender", true).enumIs(p, Page.RENDER)
    private var fadeRender = bsetting("FadeRender", false).enumIs(p, Page.RENDER)
    private var fadeAlpha = isetting("FadeAlpha", 80, 0, 255, 1).isTrue(fadeRender).enumIs(p, Page.RENDER)
    private var fillColor = csetting("FillColor", Color(20, 225, 219, 50)).enumIs(p, Page.RENDER)
    private var outlineColor = csetting("LineColor", Color(20, 225, 219, 200)).enumIs(p, Page.RENDER)
    private val movingLength = isetting("MovingLength", 400, 0, 1000).enumIs(p, Page.RENDER)
    private val fadeLength = isetting("FadeLength", 200, 0, 1000).enumIs(p, Page.RENDER)
    private var offsetFacing = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
    private var renderQueue = Object2ObjectArrayMap<BlockPos, CrystalFadeRender>().synchronized()
    private var blockBoostList = Object2ObjectArrayMap<BlockPos, Priority>().synchronized()
    private val attackedCrystalMap = Int2LongMaps.synchronize(Int2LongOpenHashMap())
    private var crystalList = CopyOnWriteArrayList<EndCrystalEntity>()
    private var ddosQueue = ConcurrentLinkedDeque<BlockPos>()

    private var packetExplodeTimer = TimerUtils()
    private var packetBypassTimer = TimerUtils()
    private val timeoutTimer = TimerUtils()
    private var explodeTimer = TimerUtils()
    private var placeTimer = TimerUtils()
    private var calcTimer = TimerUtils()
    private var fpTimer = TimerUtils()

    private var crystalInteracting: EndCrystalEntity? = null
    private var weaponSlot: HotbarSlot? = null
    private var obiSlot: HotbarSlot? = null
    private var cSlot: HotbarSlot? = null
    private var render: BlockPos? = null
    private var isFacePlacing = false
    private var bypassPacket = false
    private var ddosArmor = false
    private var flagged = false
    private var damageCA = 0.0
    private var crystalState = AtomicReference<CurrentState>().apply { CurrentState.Waiting }
    private var rotationInfo = RotationInfo(Vec2f.ZERO)
    private var renderEnt: LivingEntity? = null
    var crystalPriority = Priority.Crystal
    var placeInfo: PlaceInfo? = null

    //RenderNew
    private var lastBlockPos: BlockPos? = null
    private var lastRenderPos: Vec3d? = null
    private var prevPos: Vec3d? = null
    private var currentPos: Vec3d? = null
    private var lastTargetDamage = 0.0
    private var lastUpdateTime = 0L
    private var startTime = 0L
    private var scale = 0.0f

    private val rawPosList = CachedValueN(50L) {
        runSafe {
            getRawPosList()
        } ?: emptyList()
    }

    init {
        onRender3D {
            onRender3D(it, placeInfo)
        }

        onPacketSend { event ->
            when (event.packet) {
                is PlayerInteractEntityC2SPacket -> {
                    world.getEntityById(event.packet.entityId)?.let {
                        if (it is EndCrystalEntity) {
                            crystalInteracting = it
                        }
                    }
                }

                is PlayerActionC2SPacket -> {
                    val action = event.packet.action
                    if (action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK || action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                        doAntiSurround(event.packet.pos)
                    }
                }

            }
        }

        onPacketReceive { event ->
            when (event.packet) {
                is PlaySoundS2CPacket -> {
                    val placeInfo = placeInfo
                    if (event.packet.sound === SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        if (placeInfo != null) {
                            placeInfo.let {
                                if (distanceSq(
                                        placeInfo.blockPos.x + 0.5,
                                        placeInfo.blockPos.y + 1.0,
                                        placeInfo.blockPos.z + 0.5,
                                        event.packet.x,
                                        event.packet.y,
                                        event.packet.z
                                    ) <= 144.0
                                ) {
                                    if (packetPlace0.onRemove) {
                                        doPlace(it.blockPos) {
                                            doRotate(CurrentState.Placing)
                                        }
                                    }
                                    attackedCrystalMap.clear()
                                }
                            }
                        } else if (player.distanceSqTo(event.packet.x, event.packet.y, event.packet.z) <= 144.0) {
                            attackedCrystalMap.clear()
                        }
                    }
                }
            }
        }

        safeConcurrentListener<RunGameLoopEvent.Tick> {
            if (returnNeeded()) return@safeConcurrentListener
            rawPosList.updateForce()
            placeInfo = calcPlaceInfo()
            crystalList = CopyOnWriteArrayList(getCrystalList())
        }

        safeConcurrentListener<PlayerMotionEvent> {
            if (returnNeeded()) return@safeConcurrentListener
            updateFade(render)
            packetPlace0 = (packetPlaceMode.value as PacketPlaceMode)
            switchMode = (switchMode0.value as Switch)
            weaponSlot = getWeaponSlot()
            cSlot = if (switchMode.onPickSilent) player.allSlots.firstItem(Items.END_CRYSTAL)?.let { HotbarSlot(it) }
                else player.hotbarSlots.firstItem(Items.END_CRYSTAL)
            obiSlot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)
            if (packetBypassTimer.tickAndReset(packetDelay * (20f / TpsCalculator.tickRate)) && packetSync) {
                bypassPacket = true
                if (debug.value) ChatUtil.sendMessage("BypassPacket Synced")
            }
            if (timeoutTimer.tickAndReset(5L)) {
                updateTimeouts()
            }
            placeInfo?.let { placeInfo ->
                placeInfo.target.let { target ->
                    if (target.isAlive && (!ddosArmor || System.currentTimeMillis() - CombatManager.getHurtTime(
                            target
                        ) !in 450L..500L)
                    ) {
                        doRotate()
                        if (explodeMode.value == ExplodeMode.Normal || explodeMode.value == ExplodeMode.Both) {
                            doBreak()
                        }
                        doPlace()
                    }
                }
            }
        }

        safeParallelListener<TickEvent.Pre> {
            if (returnNeeded()) return@safeParallelListener
            updateDdosQueue()
            blockBoost()
            for (target in EntityManager.players) {
                if (isntValid(target, placeRange.value, old.value, wallRange.value)) continue
                if (PacketMine.isEnabled) {
                    PacketMine.blockData?.let {
                        val holeInfo = HoleManager.getHoleInfo(target)
                        if ((holeInfo.isHole && holeInfo.surroundPos.contains(it.blockPos)) || (canBreak(
                                target.blockPos, false
                            ) && it.blockPos == target.blockPos)
                        ) {
                            doAntiSurround(it.blockPos)
                        }
                    }
                }
            }
        }

        safeEventListener<WorldEvent.ClientBlockUpdate>(114514) {
            if (player.distanceSqToCenter(it.pos) < (placeRange.value.ceilToInt() + 1).sq && isResistant(it.oldState) != isResistant(
                    it.newState
                )
            ) {
                rawPosList.updateLazy()
                placeInfo = null
            }
        }

        safeEventListener<WorldEvent.Entity.Add> { event ->
            if (flagged || event.entity !is EndCrystalEntity || placeInfo == null) return@safeEventListener
            ddosQueue.peekFirst()?.let {
                placeInfo?.let { placeInfo ->
                    if (placeInfo.blockPos == it && armorDdos.value) {
                        if (debug.value) {
                            ChatUtil.sendNoSpamMessage("DDOS")
                        }
                        ddosQueue.pollFirst()
                    }
                }
            }
            if (explodeMode.value == ExplodeMode.Sync || explodeMode.value == ExplodeMode.Both) {
                if ((packetExplodeTimer.tickAndReset(packetDelay) && !packetSync) || (packetSync && bypassPacket)) {
                    placeInfo?.let {
                        if (crystalPlaceBoxIntersectsCrystalBox(
                                it.blockPos, event.entity.x, event.entity.y, event.entity.z
                            ) || checkBreakDamage(event.entity.x, event.entity.y, event.entity.z, BlockPos.Mutable())
                        ) {
                            breakDirect(event.entity.x, event.entity.y, event.entity.z, event.entity, false)
                            if (debug.value) {
                                ChatUtil.sendMessage("PacketExplode")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.returnNeeded(): Boolean {
        return if (eatingPause.value && player.isUsingItem) {
            placeInfo = null
            crystalList.clear()
            true
        } else false
    }

    private fun SafeClientEvent.doRotate(tempState: CurrentState? = null, tempPos: BlockPos? = null) {
        tempState?.let {
            crystalState.set(it)
        }
        val rotation = when (crystalState.get()) {
            CurrentState.Placing -> {
                tempPos?.let {
                    getLegitRotations(it.toVec3dCenter())
                } ?: placeInfo?.let {
                    getLegitRotations(it.hitVec)
                }
            }

            CurrentState.Breaking -> {
                placeInfo?.let {
                    getLegitRotations(it.hitVec)
                } ?: crystalInteracting?.let {
                    getLegitRotations(it.pos)
                }
            }

            CurrentState.Blocking -> {
                tempPos?.let {
                    getLegitRotations(tempPos.toVec3dCenter())
                }
            }

            else -> {
                rotationInfo.reset()
                null
            }
        }
        rotation?.let {
            val diff = RotationUtils.calcAngleDiff(it.x, CrystalManager.rotation.x)
            if (rotate.value) {
                rotationInfo.update(rotation)
                if (abs(diff) <= yawSpeed.value) {
                    RotationManager.addRotations(it)
                } else {
                    val clamped = diff.coerceIn(-yawSpeed.value, yawSpeed.value)
                    val newYaw = normalizeAngle(CrystalManager.rotation.x + clamped)
                    RotationManager.addRotations(newYaw, it.y)
                }
                flagged = rotateDiff.value > 0 && abs(diff) > rotateDiff.value
            }
        }
    }

    private fun SafeClientEvent.getLegitRotations(vec3d: Vec3d): Vec2f {
        val vec = vec3d.subtract(player.eyePosition)
        val xz = hypot(vec.x, vec.z)
        val yaw = normalizeAngle(atan2(vec.z, vec.x).toDegree() - 90.0)
        val pitch = normalizeAngle(-atan2(vec.y, xz).toDegree())
        return Vec2f(yaw, pitch)
    }

    private fun SafeClientEvent.doAntiSurround(pos: BlockPos?) {
        if (antiSurround.value) {
            if (EntityManager.players.isEmpty()) return
            if (pos == null) return
            for (target in EntityManager.players) {
                if (isntValid(target, placeRange.value, old.value, wallRange.value)) continue
                if (target.pos.y != pos.y.toDouble()) continue
                val burrowPos = target.blockPos
                val holeInfo = HoleManager.getHoleInfo(target)
                val finalPos = if (canBreak(burrowPos, false)) {
                    burrowPos
                } else {
                    pos
                }
                val isBurrowPos = finalPos == burrowPos
                for (facing in offsetFacing) {
                    if (!holeInfo.isHole && !isBurrowPos) continue
                    val placePos = finalPos.offset(facing)
                    if (!getAntiSurroundPos(placePos)) continue
                    if (!world.noCollision(placePos)) continue
                    if (debug.value) {
                        ChatUtil.sendMessage("AntiSurrounding")
                    }
                    doPlace(placePos.down()) {
                        doRotate(CurrentState.Placing, placePos.down())
                    }
                    render = placePos.down()
                    break
                }
            }
        }
    }

    private fun SafeClientEvent.getAntiSurroundPos(posOffset: BlockPos): Boolean {
        return world.isAir(posOffset) && canPlaceCrystal(posOffset.down(), oldPlace = old.value)
    }

    private fun SafeClientEvent.blockBoost() {
        if (blockBoost.value) {
            if (EntityManager.players.isEmpty()) return
            for (target in EntityManager.players) {
                if (isntValid(target, placeRange.value, old.value, wallRange.value)) continue
                val calcOffset = target.blockPos

                for (facing in offsetFacing) {
                    if (obiSlot == null) break
                    val placePos = calcOffset.offset(facing).down()
                    if (world.isAir(placePos) && blockBoostList.size <= 8 && !blockBoostList.contains(placePos)) {
                        blockBoostList[placePos] = Priority.Block
                    }

                    if (blockBoostList.isNotEmpty()) {
                        for ((pos: BlockPos, prio: Priority) in blockBoostList) {
                            if (!world.isAir(pos.up())) continue

                            val box = Box(
                                pos.up().x.toDouble(),
                                pos.up().y + 1.0,
                                pos.up().z.toDouble(),
                                pos.up().x + 1.0,
                                pos.up().y + 3.0,
                                pos.up().z + 1.0
                            )
                            for (entity in world.entities) {
                                if (entity is EndCrystalEntity) continue
                                if (entity.boundingBox.intersects(box)) {
                                    blockBoostList.remove(pos)
                                    continue
                                }
                            }
                            val predictDamage = calcDamage(
                                target,
                                target.pos,
                                target.boundingBox,
                                pos.up().x + 0.5,
                                (pos.up().y + 1).toDouble(),
                                pos.up().z + 0.5,
                                BlockPos.Mutable(),
                                true
                            ).toDouble()

                            val placeInfo = placeInfo
                            if (placeInfo != null) {
                                if (predictDamage <= placeInfo.targetDamage) {
                                    blockBoostList.remove(pos)
                                    ChatUtil.sendMessage("Filtered Lower Damage!")
                                    continue
                                } else {
                                    ChatUtil.sendMessage("Block Boosted PlaceInfo!")
                                    break
                                }
                            } else {
                                if (world.isAir(pos)) {
                                    crystalPriority = prio
                                    if (!world.noCollision(pos)) continue
                                    obiSlot?.let {
                                        doRotate(CurrentState.Blocking, pos)
                                        when (switchMode0.value) {
                                            Switch.SpoofBypass -> {
                                                spoofHotbarBypass(it) {
                                                    sendSequencedPacket(world) { sequence ->
                                                        fastPos(pos, strictDirection.value, sequence = sequence)
                                                    }
                                                }
                                            }

                                            else -> {
                                                spoofHotbar(it) {
                                                    sendSequencedPacket(world) { sequence ->
                                                        fastPos(pos, strictDirection.value, sequence = sequence)
                                                    }
                                                }
                                            }
                                        }
                                        render = pos
                                    }
                                    crystalPriority = Priority.Crystal
                                    blockBoostList.clear()
                                    if (debug.value) {
                                        ChatUtil.sendMessage("Block Boosted!")
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateTimeouts() {
        val current = System.currentTimeMillis()
        attackedCrystalMap.runSynchronized {
            values.removeIf {
                it < current
            }
        }
    }

    private fun checkPlaceCollision(placeInfo: PlaceInfo): Boolean {
        return EntityManager.entity.asSequence().filterIsInstance<EndCrystalEntity>().filter { it.isAlive }
            .filter { crystalPlaceBoxIntersectsCrystalBox(placeInfo.blockPos, it) }
            .filterNot { attackedCrystalMap.containsKey(it.id) }.none()
    }

    private fun SafeClientEvent.doPlace(tempPos: BlockPos? = null, rotationInvoke: (() -> Unit)? = null) {
        if (tempPos == null) {
            val normal = placeInfo
            render = if (armorDdos.value && (normal == null || normal.targetDamage < placeMinDmg.value)) {
                if (debug.value) {
                    ChatUtil.sendMessage("DDOS Try!")
                }
                ddosQueue.peekFirst()
            } else {
                normal?.blockPos
            }
            renderEnt = normal?.target
        } else {
            render = tempPos
        }
        if ((renderEnt == null && tempPos == null) || render == null || crystalPriority != Priority.Crystal) {
            renderEnt = null
            render = null
            crystalState.set(CurrentState.Waiting)
            return
        }
        rotationInvoke?.invoke() ?: doRotate(CurrentState.Placing)
        if (slowFP.value && isFacePlacing && renderEnt != null) {
            if (!fpTimer.passed(fpDelay.value)) {
                return
            }
            fpTimer.reset()
        }
        placeInfo?.let { placeInfo ->
            fun SafeClientEvent.sendPacket() {
                sendSequencedPacket(world) {
                    placePacket(
                        placeInfo,
                        if (player.offHandStack.item == Items.END_CRYSTAL) Hand.OFF_HAND else Hand.MAIN_HAND,
                        it
                    )
                }
            }
            if (placeTimer.tickAndReset(placeDelay.value)) {
                onMainThread {
                    if (player.offHandStack.item == Items.END_CRYSTAL) {
                        sendPacket()
                    } else {
                        if (switchMode.onSwitch) {
                            val slot = player.getCrystalSlot() ?: return@onMainThread
                            if (player.mainHandStack.item is EnchantedGoldenAppleItem && player.usingItem && player.offHandStack.item != Items.END_CRYSTAL) {
                                return@onMainThread
                            }
                            swapToSlot(slot)
                            sendPacket()
                        } else if (switchMode.onSpoof) {
                            val slot = player.getCrystalSlot() ?: return@onMainThread
                            spoofHotbar(slot) {
                                sendPacket()
                            }
                        } else if (switchMode.onBypassSpoof) {
                            val slot = player.getMaxCrystalSlot() ?: return@onMainThread
                            spoofHotbarBypass(slot) {
                                sendPacket()
                            }
                        } else if (switchMode.onPickSilent) {
                            val slot = player.allSlots.firstItem(Items.END_CRYSTAL)?.let { HotbarSlot(it) } ?: return@onMainThread
                            spoofInvBypass(slot) {
                                sendPacket()
                            }
                        } else {
                            sendPacket()
                        }
                    }
                }
                if (placeSwing.value) {
                    onMainThread {
                        swingArm()
                    }
                }
            }
        }
    }

    private fun placePacket(placeInfo: PlaceInfo, hand: Hand, sequence: Int): PlayerInteractBlockC2SPacket {
        return PlayerInteractBlockC2SPacket(
            hand, BlockHitResult(placeInfo.hitVec, placeInfo.side, placeInfo.blockPos, false), sequence
        )
    }

    fun SafeClientEvent.getPlaceSide(pos: BlockPos): Direction {
        return if (strictDirection.value) {
            getMiningSide(pos) ?: Direction.UP
        } else {
            Direction.UP
        }
    }

    private fun SafeClientEvent.calcPlaceInfo(): PlaceInfo? {
        val placeInfo: PlaceInfo.Mutable?
        val normal = PlaceInfo.Mutable(player)
        val targets = targetList.toList()
        if (targets.isEmpty()) return null

        val context = CombatManager.contextSelf ?: return null

        val mutableBlockPos = BlockPos.Mutable()
        val targetBlocks = rawPosList.get()

        if (targetBlocks.isEmpty()) {
            return null
        }

        if (cSlot == null && player.offHandStack.item != Items.END_CRYSTAL) {
            return null
        }
        for (blockPos in targetBlocks) {
            val placeBox = CrystalUtils.getCrystalPlacingBB(blockPos)
            val crystalX = blockPos.x + 0.5
            val crystalY = blockPos.y + 1.0
            val crystalZ = blockPos.z + 0.5
            val selfDamage = max(
                context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos),
                context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos)
            ).toDouble()
            val collidingDamage = calcCollidingCrystalDamage(placeBox)
            if (player.scaledHealth - selfDamage <= noSuicide.value) continue
            if (player.scaledHealth - collidingDamage <= noSuicide.value) continue

            for ((target, targetPos, targetBox, currentPos) in targets) {
                if (target != player && target is LivingEntity) {
                    if (targetBox.intersects(placeBox)) continue
                    if (placeBox.intersects(targetPos, currentPos)) continue

                    val targetDamage = calcDamage(
                        target,
                        targetPos,
                        targetBox,
                        blockPos.x + 0.5,
                        (blockPos.y + 1).toDouble(),
                        blockPos.z + 0.5,
                        mutableBlockPos,
                        true
                    ).toDouble()
                    if (selfDamage > placeMaxSelf.value) continue

                    damageCA = targetDamage
                    val holeInfo = HoleManager.getHoleInfo(target)
                    isFacePlacing = holeInfo.isHole && !ddosArmor

                    val minDamage: Double
                    val balance: Float

                    if (shouldForcePlace(target)) {
                        minDamage = forcePlaceDmg.value
                        balance = forcePlaceBalance.value
                    } else {
                        minDamage = placeMinDmg.value
                        balance = placeBalance.value
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > normal.targetDamage) {
                            normal.update(target, blockPos, selfDamage, targetDamage)
                        }
                    }

                    if (!checkPlaceCollision(normal)) continue
                }
            }
        }

        placeInfo = normal.takeValid()
        placeInfo?.calcPlacement(this)
        return placeInfo
    }

    private fun SafeClientEvent.shouldForcePlace(entity: LivingEntity): Boolean {
        return player.mainHandStack.item !is SwordItem && (entity.totalHealth <= forceHealth.value || entity.realSpeed >= forcePlaceMotion.value || entity.getMinArmorRate() <= armorRate.value)
    }

    private fun LivingEntity.getMinArmorRate(): Int {
        var minDura = 100

        for (armor in armorItems.toList()) {
            if (!armor.isDamageable) continue
            val dura = armor.duraPercentage
            if (dura < minDura) {
                minDura = dura
            }
        }

        return minDura
    }

    private fun SafeClientEvent.swingArm() {
        when (swingMode.value) {
            SwingMode.Offhand -> {
                player.swingHand(Hand.OFF_HAND)
            }

            SwingMode.Mainhand -> {
                player.swingHand(Hand.MAIN_HAND)
            }

            SwingMode.Auto -> {
                player.swingHand(if (player.offHandStack.item == Items.END_CRYSTAL) Hand.OFF_HAND else Hand.MAIN_HAND)
            }
        }
    }

    private val SafeClientEvent.targetList: Sequence<TargetInfo>
        get() {
            val rangeSq = enemyRange.value.sq
            val ticks = if (motionPredict.value) predictTicks.value else 0
            val list = ObjectArrayList<TargetInfo>().synchronized()
            val eyePos = CrystalManager.eyePosition

            for (target in EntityManager.players) {
                if (target == player) continue
                if (!target.isAlive) continue
                if (target.distanceSqTo(eyePos) > rangeSq) continue
                if (isFriend(target.entityName)) continue

                list.add(getPredictedTarget(target, ticks))
            }

            return list.asSequence().filter { it.entity.isAlive }
                .sortedWith(compareByDescending<TargetInfo> { (it.entity as LivingEntity).scaledHealth }.thenBy {
                    player.distanceSqTo(
                        it.predictMotion
                    )
                }).take(maxTargets.value)
        }

    private fun SafeClientEvent.doBreak() {
        val crystal = getTargetCrystal(crystalList) ?: getCrystal(crystalList) ?: getFinalCrystal(crystalList)

        crystal?.let {
            if (!flagged && explodeTimer.tickAndReset(hitDelay.value)) {
                breakDirect(it.x, it.y, it.z, it, true)
            }
        }
    }

    private fun SafeClientEvent.breakDirect(
        x: Double, y: Double, z: Double, endCrystal: EndCrystalEntity, packetPrio: Boolean
    ) {
        if (player.isWeaknessActive() && !isHoldingTool()) {
            weaponSlot?.let {
                onMainThread {
                    when (antiWeakness.value) {
                        AntiWeaknessMode.Off -> {
                            return@onMainThread
                        }

                        AntiWeaknessMode.Swap -> {
                            swapToSlot(it)
                            runExplode(endCrystal, packetPrio)
                            swingArm()
                        }

                        AntiWeaknessMode.Spoof -> {
                            spoofHotbar(it) {
                                runExplode(endCrystal, packetPrio)
                                swingArm()
                            }
                        }

                        AntiWeaknessMode.Bypass -> {
                            spoofHotbarBypass(it) {
                                runExplode(endCrystal, packetPrio)
                                swingArm()
                            }
                        }
                    }
                }
            }
        } else {
            runExplode(endCrystal, packetPrio)
            swingArm()
        }

        placeInfo?.let {
            if (packetPlace0.onBreak && crystalPlaceBoxIntersectsCrystalBox(it.blockPos, x, y, z)) {
                doPlace(it.blockPos) {
                    doRotate(CurrentState.Placing, it.blockPos)
                }
            }
        }
        attackedCrystalMap[id] = System.currentTimeMillis() + 1000L
    }

    private fun SafeClientEvent.getFinalCrystal(crystalList: List<EndCrystalEntity>): EndCrystalEntity? {
        return crystalList.filter { checkBreakDamage(it.x, it.y, it.z, BlockPos.Mutable()) }
            .minByOrNull { player.distanceSqToCenter(it.blockPos) }
    }

    private fun getTargetCrystal(crystalList: List<EndCrystalEntity>): EndCrystalEntity? {
        placeInfo?.let { placeInfo ->
            return crystalList.firstOrNull {
                crystalPlaceBoxIntersectsCrystalBox(placeInfo.blockPos, it.x, it.y, it.z)
            }
        } ?: return null
    }

    private fun SafeClientEvent.getCrystalList(): List<EndCrystalEntity> {
        return EntityManager.entity.asSequence().filterIsInstance<EndCrystalEntity>().filter { it.isAlive }.filter {
            checkBreakRange(
                it, breakRange.value, old.value, wallRange.value
            )
        }.toList()
    }

    private fun SafeClientEvent.getCrystal(crystalList: List<EndCrystalEntity>): EndCrystalEntity? {
        val max = BreakInfo.Mutable()
        val targets = targetList.toList()

        val noSuicide = noSuicide.value
        val mutableBlockPos = BlockPos.Mutable()
        val context = CombatManager.contextSelf ?: return null

        if (targets.isNotEmpty()) {
            for (crystal in crystalList) {
                val selfDamage = max(
                    context.calcDamage(crystal.x, crystal.y, crystal.z, false, mutableBlockPos),
                    context.calcDamage(crystal.x, crystal.y, crystal.z, true, mutableBlockPos)
                )
                if (player.scaledHealth - selfDamage <= noSuicide) continue

                for ((entity, entityPos, entityBox) in targets) {
                    if (entity !is LivingEntity) continue
                    val targetDamage = calcDamage(
                        entity, entityPos, entityBox, crystal.x, crystal.y, crystal.z, mutableBlockPos, true
                    ).toDouble()

                    if (selfDamage > breakMaxSelf.value) continue

                    val minDamage: Double
                    val balance: Float

                    if (shouldForcePlace(entity)) {
                        minDamage = forcePlaceDmg.value
                        balance = forcePlaceBalance.value
                    } else {
                        minDamage = breakMinDmg.value
                        balance = breakBalance.value
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > max.targetDamage) {
                            max.update(crystal, selfDamage, targetDamage)
                        }
                    }
                }
            }
        }

        val valid = max.takeValid()

        return valid?.crystal
    }

    private fun SafeClientEvent.runExplode(
        endCrystal: EndCrystalEntity, packetPrio: Boolean = true, rotationInvoke: (() -> Unit)? = null
    ) {
        runCatching {
            onMainThread {
                crystalState.set(CurrentState.Breaking)
                rotationInvoke?.invoke()
                connection.sendPacket(PlayerInteractEntityC2SPacket.attack(endCrystal, player.isSneaking))
                if (bypassPacket && packetSync && packetPrio) {
                    if (debug.value) ChatUtil.sendMessage("BypassPacket Reset")
                    packetBypassTimer.reset()
                    bypassPacket = false
                }
                swingArm()
            }
        }
    }

    private fun onRender3D(event: Render3DEvent, placeInfo: PlaceInfo?) {
        val filled = fillColor.value.alpha > 0
        val outline = outlineColor.value.alpha > 0
        val flag = filled || outline

        if (!fadeRender.value) {
            if (flag) {
                try {
                    update(placeInfo)
                    scale = if (placeInfo != null) {
                        Easing.OUT_CUBIC.inc(Easing.toDelta(startTime, fadeLength.value))
                    } else {
                        Easing.IN_CUBIC.dec(Easing.toDelta(startTime, fadeLength.value))
                    }

                    prevPos?.let { prevPos ->
                        currentPos?.let { currentPos ->
                            val multiplier = Easing.OUT_QUART.inc(Easing.toDelta(lastUpdateTime, movingLength.value))
                            val motionRenderPos =
                                prevPos.add(currentPos.subtract(prevPos).multiply(multiplier.toDouble()))
                            val staticRenderPos = currentPos

                            val finalPos = if (motionRender.value) motionRenderPos else staticRenderPos
                            val box = toRenderBox(finalPos, if (motionRender.value) scale else 1f)
                            val renderer = ESPRenderer()

                            renderer.aFilled = (fillColor.value.alpha * scale).toInt()
                            renderer.aOutline = (outlineColor.value.alpha * scale).toInt()
                            renderer.add(box, fillColor.value, outlineColor.value)
                            renderer.render(event.matrices, false)
                            renderDamageText(box)

                            lastRenderPos = finalPos
                        }
                    }
                } catch (_: Exception) {
                }
            }
        } else {
            if (renderQueue.isNotEmpty()) {
                renderQueue.forEach {
                    it.value.blockPos?.let { pos ->
                        if (placeInfo != pos) {
                            if (it.value.alpha > 0) {
                                it.value.alpha -= 1
                            } else {
                                renderQueue.remove(it.key, it.value)
                            }
                        } else {
                            lastRenderPos = pos.toVec3dCenter()
                            if (it.value.alpha < it.value.oriAlpha) {
                                it.value.alpha += 1
                            }
                        }
                        val renderer = ESPRenderer()

                        renderer.aFilled = it.value.alpha
                        renderer.aOutline = it.value.alpha
                        renderer.add(pos, fillColor.value, outlineColor.value)
                        renderer.render(event.matrices, false)
                        renderDamageText(Box(pos))
                    }
                }
            }
        }
    }


    private fun renderDamageText(box: Box) {
        if (renderDamage.value && scale != 0.0f) {
            lastRenderPos?.let { lastPos ->
                val text = buildString {
                    append("%.1f".format(lastTargetDamage))
                }
                var alpha = (255.0f * scale).toInt()
                var color = if (scale == 1.0f) ColorRGB(255, 255, 255) else ColorRGB(255, 255, 255, alpha)
                if (!fadeRender.value) {
                    alpha = (255.0f * scale).toInt()
                    color = if (scale == 1.0f) ColorRGB(255, 255, 255) else ColorRGB(255, 255, 255, alpha)
                } else {
                    if (renderQueue.isNotEmpty()) {
                        renderQueue.forEach { (_: BlockPos, fade: CrystalFadeRender) ->
                            if (lastPos == fade.blockPos) {
                                alpha = fade.alpha
                                color = if (fade.alpha == fade.oriAlpha) ColorRGB(255, 255, 255) else ColorRGB(
                                    255, 255, 255, fade.alpha
                                )
                            }
                        }
                    }
                }
                Render3DEngine.drawTextIn3D(
                    text, box.center, 0.0, 0.2, 0.0, Render2DEngine.injectAlpha(Color(color.r, color.g, color.b), alpha)
                )
            }
        }
    }

    private fun toRenderBox(vec3d: Vec3d, scale: Float): Box {
        val halfSize = 0.5 * scale
        return Box(
            vec3d.x - halfSize,
            vec3d.y - halfSize,
            vec3d.z - halfSize,
            vec3d.x + halfSize,
            vec3d.y + halfSize,
            vec3d.z + halfSize
        )
    }

    private fun updateFade(blockPos: BlockPos?) {
        if (fadeRender.value) {
            blockPos?.let {
                if (!renderQueue.containsKey(it)) {
                    renderQueue[it] = CrystalFadeRender(it, fadeAlpha.value, fadeAlpha.value)
                }
            }
        }
    }

    private fun update(placeInfo: PlaceInfo?) {
        val newBlockPos = placeInfo?.blockPos
        if (newBlockPos != lastBlockPos) {
            if (newBlockPos != null) {
                currentPos = placeInfo.blockPos.toVec3dCenter()
                prevPos = lastRenderPos ?: currentPos
                lastUpdateTime = System.currentTimeMillis()
                if (lastBlockPos == null) startTime = System.currentTimeMillis()
            } else {
                lastUpdateTime = System.currentTimeMillis()
                if (lastBlockPos != null) startTime = System.currentTimeMillis()
            }

            lastBlockPos = newBlockPos
        }

        placeInfo?.let {
            lastTargetDamage = it.targetDamage
        }
    }

    private fun SafeClientEvent.checkBreakDamage(
        crystalX: Double, crystalY: Double, crystalZ: Double, mutableBlockPos: BlockPos.Mutable
    ): Boolean {
        val context = CombatManager.contextSelf ?: return false
        val selfDamage = max(
            context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos),
            context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos)
        )
        if (player.scaledHealth - selfDamage <= noSuicide.value) return false
        return targetList.toList().any {
            checkBreakDamage(crystalX, crystalY, crystalZ, selfDamage, it, mutableBlockPos)
        }
    }

    private fun SafeClientEvent.checkBreakDamage(
        crystalX: Double,
        crystalY: Double,
        crystalZ: Double,
        selfDamage: Float,
        targetInfo: TargetInfo,
        mutableBlockPos: BlockPos.Mutable
    ): Boolean {
        if (targetInfo.entity is LivingEntity) {
            val targetDamage = calcDamage(
                targetInfo.entity, targetInfo.pos, targetInfo.box, crystalX, crystalY, crystalZ, mutableBlockPos, true
            )

            if (selfDamage > breakMaxSelf.value) return false

            val minDamage: Double
            val balance: Float

            if (shouldForcePlace(targetInfo.entity)) {
                minDamage = forcePlaceDmg.value
                balance = forcePlaceBalance.value
            } else {
                minDamage = breakMinDmg.value
                balance = breakBalance.value
            }

            return targetDamage >= minDamage && targetDamage - selfDamage >= balance
        } else if (targetInfo.entity is ItemEntity) {
            return true
        }
        return false
    }

    private fun ClientPlayerEntity.isWeaknessActive(): Boolean {
        return this.getStatusEffect(StatusEffects.WEAKNESS) != null && this.getStatusEffect(StatusEffects.STRENGTH)
            ?.let {
                it.amplifier <= 0
            } ?: true
    }

    private fun SafeClientEvent.isHoldingTool(): Boolean {
        val item = player.serverSideItem.item
        return item is SwordItem || item is ToolItem
    }

    private fun SafeClientEvent.getWeaponSlot(): HotbarSlot? {
        return player.hotbarSlots.filterByStack {
            val item = it.item
            item is SwordItem || item is ToolItem
        }.maxByOrNull {
            val itemStack = it.stack
            itemStack.attackDamage
        }
    }

    private fun SafeClientEvent.getRawPosList(prio: Priority = Priority.Crystal): List<BlockPos> {
        val positions = CopyOnWriteArrayList<BlockPos>()

        positions.addAll(SphereCalculatorManager.sphereList.stream().filter {
            world.isInBuildLimit(it) && world.worldBorder.contains(it)
        }.filter {
            val crystalX = it.x + 0.5
            val crystalY = it.y + 1.0
            val crystalZ = it.z + 0.5
            player.distanceSqTo(
                crystalX, crystalY, crystalZ
            ) <= placeRange.value.sq && (!old.value || player.distanceSqTo(it.toCenterPos()) <= wallRange.value || canSee(
                it.x.toDouble(), it.y.toDouble(), it.z.toDouble()
            ))
        }.filter { canPlaceCrystal(it, prio, old.value) }.collect(Collectors.toList())
        )

        return positions
    }

    private fun SafeClientEvent.canPlaceCrystal(
        blockPos: BlockPos, priority: Priority = Priority.Crystal, oldPlace: Boolean = false
    ): Boolean {
        val boost = blockPos.add(0, if (priority == Priority.Block) 2 else 1, 0)
        val base = world.getBlockState(blockPos).block
        val b1 = world.getBlockState(boost).block
        if (blockBoost.value && priority == Priority.Block) {
            return true
        } else if (base !== Blocks.BEDROCK && base !== Blocks.OBSIDIAN) {
            return false
        }
        if (b1 !== Blocks.AIR && !isReplaceable(b1)) return false
        if (!world.isAir(blockPos.up(2)) && oldPlace) return false
        val box = Box(
            blockPos.x.toDouble(),
            blockPos.y + 1.0,
            blockPos.z.toDouble(),
            blockPos.x + 1.0,
            blockPos.y + 3.0,
            blockPos.z + 1.0
        )
        val upBox = Box(
            blockPos.up().x.toDouble(),
            blockPos.up().y + 1.0,
            blockPos.up().z.toDouble(),
            blockPos.up().x + 1.0,
            blockPos.up().y + 3.0,
            blockPos.up().z + 1.0
        )
        for (entity in world.entities) {
            if (entity is EndCrystalEntity) continue
            if (entity.boundingBox.intersects(box)) return false
            if (entity.boundingBox.intersects(upBox)) return false
        }
        return base !is FireBlock
    }

    private fun SafeClientEvent.updateDdosQueue() {
        val target = placeInfo?.target
        val mutableBlockPos = BlockPos.Mutable()
        val placeList = rawPosList.get()
        ddosArmor = armorDdos.value && target != null && placeInfo.let {
            (it == null || it.targetDamage < placeMinDmg.value)
        }

        if (target == null || !ddosArmor) {
            ddosQueue.clear()
            return
        }

        val diff = System.currentTimeMillis() - CombatManager.getHurtTime(target)

        if (diff > 500L) {
            if (ddosArmor && ddosQueue.isEmpty() && shouldForcePlace(target)) {
                val last = 0f

                if (last < placeMinDmg.value) {
                    val contextSelf = CombatManager.contextSelf ?: return
                    val sequence = placeList.asSequence().filter {
                        calcDamage(
                            target,
                            target.pos,
                            target.boundingBox,
                            it.x + 0.5,
                            (it.y + 1).toDouble(),
                            it.z + 0.5,
                            mutableBlockPos,
                            true
                        ).toDouble() > ddosMinDamage.value
                    }.filter {
                        player.scaledHealth - max(
                            contextSelf.calcDamage(
                                it.x + 0.5, (it.y + 1).toDouble(), it.z + 0.5, false, mutableBlockPos
                            ), contextSelf.calcDamage(
                                it.x + 0.5, (it.y + 1).toDouble(), it.z + 0.5, true, mutableBlockPos
                            )
                        ) > noSuicide.value
                    }

                    ddosQueue.clear()
                    var lastDamage = Int.MAX_VALUE

                    for (crystalDamage in sequence) {
                        val targetDamage = calcDamage(
                            target,
                            target.pos,
                            target.boundingBox,
                            crystalDamage.x + 0.5,
                            (crystalDamage.y + 1).toDouble(),
                            crystalDamage.z + 0.5,
                            mutableBlockPos,
                            true
                        ).toDouble()
                        val roundedDamage = (targetDamage / ddosDamageStep.value).roundToInt()
                        if (lastDamage == roundedDamage || lastDamage - roundedDamage < ddosDamageStep.value) continue
                        ddosQueue.addFirst(crystalDamage)
                        lastDamage = roundedDamage

                        if (ddosQueue.size >= ddosQueueSize.value) break
                    }
                }
            }
        } else if (diff > 450L) {
            ddosQueue.clear()
        }
    }

    override fun onEnable() {
        runSafe {
            cSlot = null
            isFacePlacing = false
            bypassPacket = false
            flagged = false
            packetExplodeTimer.reset()
            packetBypassTimer.reset()
            explodeTimer.reset()
            placeTimer.reset()
            calcTimer.reset()
            fpTimer.reset()
        }
    }

    override fun onDisable() {
        runSafe {
            renderQueue.clear()
            ddosQueue.clear()
            renderEnt = null
            render = null
            prevPos = null
            currentPos = null
            lastRenderPos = null
            lastBlockPos = null
            lastTargetDamage = 0.0
            lastUpdateTime = 0L
            startTime = 0L
            scale = 0.0f
        }
    }

    override fun getHudInfo(): String {
        return renderEnt?.let {
            ChatUtil.GREEN + ChatUtil.BOLD + it.name.string
        } ?: ""
    }

    @Suppress("unused")
    enum class PacketPlaceMode(override val displayName: CharSequence, val onRemove: Boolean, val onBreak: Boolean): DisplayEnum {
        Off("Off", false, false),
        Weak("Weak", true, false),
        Strong("Strong", true, true)
    }

    @Suppress("unused")
    enum class Switch(override val displayName: CharSequence, val onSpoof: Boolean, val onSwitch: Boolean, val onBypassSpoof: Boolean, val onPickSilent: Boolean): DisplayEnum {
        AutoSwitch("AutoSwitch", false, true, false, false),
        PacketSpoof("PacketSpoof", true, false, false, false),
        SpoofBypass("SpoofBypass", false, false, true, false),
        PickSpoof("PickSpoof", false, false, false, true),
        Off("Off", false, false, false, false)
    }

    enum class DamageMode(override val displayName: CharSequence): DisplayEnum {
        Auto("Auto"),
        PPBP("PPBP"),
        BBBB("BBBB")
    }

    enum class Page(override val displayName: CharSequence): DisplayEnum {
        GENERAL("General"),
        CALCULATION("Calculation"),
        PLACE("Place"),
        BREAK("Break"),
        FORCE("Force"),
        LETHAL("Lethal"),
        RENDER("Render")
    }

    enum class AntiWeaknessMode(override val displayName: CharSequence): DisplayEnum {
        Swap("Swap"),
        Spoof("Spoof"),
        Bypass("Bypass"),
        Off("Off")
    }

    enum class ExplodeMode(override val displayName: CharSequence): DisplayEnum {
        Normal("Normal"),
        Sync("Sync"),
        Both("Both")
    }

    enum class SwingMode(override val displayName: CharSequence): DisplayEnum {
        Offhand("Offhand"),
        Mainhand("Mainhand"),
        Auto("Auto"),
        Off("Off")
    }

    enum class CurrentState {
        Placing,
        Breaking,
        Blocking,
        Waiting
    }

    enum class Priority {
        Block,
        Crystal
    }

    private fun SafeClientEvent.getPredictedTarget(entity: PlayerEntity, ticks: Int): TargetInfo {
        val motionX = (entity.x - entity.lastRenderX).coerceIn(-0.6, 0.6)
        val motionY = (entity.y - entity.lastRenderY).coerceIn(-0.5, 0.5)
        val motionZ = (entity.z - entity.lastRenderZ).coerceIn(-0.6, 0.6)

        val entityBox = entity.boundingBox
        var targetBox = entityBox

        for (tick in 0..ticks) {
            targetBox =
                canMove(targetBox, motionX, motionY, motionZ) ?: canMove(targetBox, motionX, 0.0, motionZ) ?: canMove(
                    targetBox, 0.0, motionY, 0.0
                ) ?: break
        }

        val offsetX = targetBox.minX - entityBox.minX
        val offsetY = targetBox.minY - entityBox.minY
        val offsetZ = targetBox.minZ - entityBox.minZ
        val motion = Vec3d(offsetX, offsetY, offsetZ)
        val pos = entity.pos

        return TargetInfo(
            entity,
            pos.add(motion),
            targetBox,
            pos,
            motion,
            ExposureSample.getExposureSample(entity.width, entity.height)
        )
    }

    data class TargetInfo(
        val entity: Entity,
        val pos: Vec3d,
        val box: Box,
        val currentPos: Vec3d,
        val predictMotion: Vec3d,
        val exposureSample: ExposureSample
    )
}