package dev.zenhao.melon.module

import dev.zenhao.melon.Melon
import dev.zenhao.melon.module.hud.*
import dev.zenhao.melon.module.modules.client.*
import dev.zenhao.melon.module.modules.combat.*
import dev.zenhao.melon.module.modules.crystal.MelonAura2
import dev.zenhao.melon.module.modules.misc.*
import dev.zenhao.melon.module.modules.movement.*
import dev.zenhao.melon.module.modules.player.*
import dev.zenhao.melon.module.modules.player.disabler.Disabler
import dev.zenhao.melon.module.modules.render.*
import kotlinx.coroutines.async
import melon.events.TickEvent
import melon.events.input.BindEvent
import melon.system.event.AlwaysListening
import melon.system.event.listener
import melon.system.render.newfont.FontRenderers
import melon.utils.concurrent.threads.IOScope
import melon.utils.math.DamageCalculator
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

object ModuleManager : AlwaysListening {
    private var sortedModules = CopyOnWriteArrayList<AbstractModule>()
    val moduleList = CopyOnWriteArrayList<AbstractModule>()

    init {
        listener<TickEvent.Pre>(true) {
            sortModules()
            moduleList.forEach {
                if (it.bind == -1) {
                    it.bind = 0
                }
            }
        }
    }

    fun init() {
        loadModules()
        loadHUDs()
        moduleList.sortWith(Comparator.comparing { it.moduleName })
        Melon.logger.info("Module Initialised")
    }

    fun getToggleList(): ArrayList<Module> {
        val toggleList = ArrayList<Module>()
        toggleList.add(FakePlayer)
        toggleList.add(PortalESP)
        toggleList.add(Xray)
        return toggleList
    }

    private fun loadCategoryClient() {
        registerModule(ClickGui)
        registerModule(HUDEditor)
        registerModule(Colors)
        registerModule(OverrideFont)
        registerModule(UiSetting)
        registerModule(Cape)
        registerModule(LoadingMenu)
    }

    private fun loadCategoryCombat() {
        registerModule(AutoReplenish)
        registerModule(AutoTotem)
        registerModule(Surround)
        registerModule(HoleFiller)
        registerModule(CityMiner)
        registerModule(AnchorAura)
        registerModule(AutoTrap)
        registerModule(BowBomb)
        registerModule(FastUse)
        registerModule(HoleSnap)
        registerModule(SelfTrap)
        registerModule(SmartOffHand)
        registerModule(AutoEXP)
        registerModule(KillAura)
        registerModule(NewBedAura)
        registerModule(ManualCev)
        registerModule(AutoWeb)
        registerModule(HolePush)
        registerModule(PistonAura)
        registerModule(Burrow)
        registerModule(HeadTrap)
        registerModule(InfiniteAura)
        registerModule(CityRecode)
    }

    private fun loadCategoryMisc() {
        registerModule(FakePlayer)
        registerModule(AutoReconnect)
        registerModule(AutoRespawn)
        registerModule(MCF)
        registerModule(MCP)
        registerModule(TotemPopCounter)
        registerModule(AirPlace)
        registerModule(AutoCraftBed)
        registerModule(ChatSuffix)
        registerModule(PearlClip)
        registerModule(Spammer)
        registerModule(PacketLogger)
    }

    private fun loadCategoryMovement() {
        registerModule(Velocity)
        registerModule(Strafe)
        registerModule(Speed)
        registerModule(Sprint)
        registerModule(Step)
        registerModule(GUIMove)
        registerModule(NoSlowDown)
        registerModule(ElytraFly)
        registerModule(FastWeb)
        registerModule(Blink)
        registerModule(Flight)
        registerModule(ControlElytraFly)
        registerModule(AutoWalk)
    }

    private fun loadCategoryPlayer() {
        registerModule(PacketMine)
        registerModule(AutoArmour)
        registerModule(NoEntityTrace)
        registerModule(NoRotate)
        registerModule(Timer)
        registerModule(Reach)
        registerModule(PacketEat)
        registerModule(Scaffold)
        registerModule(NoFall)
        registerModule(Freecam)
        registerModule(HitboxDesync)
        registerModule(AntiMinePlace)
        registerModule(AntiHunger)
        registerModule(HotbarSwapper)
        registerModule(XCarry)
        registerModule(Disabler)
    }

    private fun loadCategoryRender() {
        registerModule(BlockHighlight)
        registerModule(NoRender)
        registerModule(CustomFov)
        registerModule(Brightness)
        registerModule(GameAnimation)
        registerModule(NameTags)
        registerModule(HoleESP)
        registerModule(ToolTips)
        registerModule(PortalESP)
        registerModule(CrystalRender)
        registerModule(LogESP)
        registerModule(Chams)
        registerModule(CameraClip)
        registerModule(HandView)
        registerModule(PlaceRender)
        registerModule(BreakESP)
        registerModule(Aspect)
        registerModule(Xray)
        registerModule(PopChams)
        registerModule(Zoom)
        registerModule(AntiAlias)
        registerModule(TargetInfo)
    }

    private fun loadModules() {
        loadCategoryCombat()
        loadCategoryClient()
        loadCategoryMisc()
        loadCategoryMovement()
        loadCategoryRender()
        loadCategoryPlayer()
        registerModule(MelonAura2)
        DamageCalculator
        getModules().sortedWith(Comparator.comparing { it.moduleName })
    }

    private fun loadHUDs() {
        registerModule(ArmourHUD)
        registerModule(CoordsHUD)
        registerModule(ModuleListHUD)
        registerModule(FpsHUD)
        registerModule(FriendListHUD)
        registerModule(PingHUD)
        registerModule(RamHUD)
        registerModule(ServerHUD)
        registerModule(SpeedHUD)
        registerModule(PlayerListHUD)
        registerModule(TpsHUD)
        registerModule(NotificationHUD)
        registerModule(WaterMarkHUD)
        registerModule(TargetHUD)
        registerModule(Image)
        getModules().sortedWith(Comparator.comparing { it.moduleName })
    }

    fun onKey(event: BindEvent) {
        moduleList.forEach {
            if (it.isEnabled) {
                it.onKey(event)
            }
        }
    }

    private fun registerModule(module: AbstractModule) = runCatching {
        IOScope.async { moduleList.add(module) }
    }

    @JvmStatic
    fun getModules(): List<AbstractModule> {
        return moduleList.stream().filter { it is Module }.collect(Collectors.toList())
    }

    private fun sortModules() {
        sortedModules = CopyOnWriteArrayList(
            getEnabledModules().stream()
                .sorted(Comparator.comparing { FontRenderers.lexend.getStringWidth(it.getArrayList()) * -1 })
                .collect(Collectors.toList())
        )
    }

    private fun getEnabledModules(): ArrayList<AbstractModule> {
        val enabledModules = ArrayList<AbstractModule>()
        for (module in moduleList) {
            if (!module.isEnabled) continue
            enabledModules.add(module)
        }
        return enabledModules
    }

    @JvmStatic
    val hUDModules: List<HUDModule>
        get() = moduleList.filterIsInstance<HUDModule>().toList()

    @JvmStatic
    fun getModuleByName(targetName: String?): Module {
        for (iModule in moduleList.filterIsInstance<Module>()) {
            if (!iModule.moduleName.equals(targetName, ignoreCase = true)) continue
            return iModule
        }
        //TODO 祖传代码👇
        //XG42.logger.fatal("Module " + targetName + " is not exist.Please check twice!");
        return NullModule
    }

    @JvmStatic
    fun getModuleByClass(targetName: Class<out Module>): Module {
        for (iModule in moduleList.filterIsInstance<Module>()) {
            if (iModule.javaClass != targetName) continue
            return iModule
        }
        //TODO 一码传三代
        //XG42.logger.fatal("Module " + targetName + " is not exist.Please check twice!");
        return NullModule
    }

    @JvmStatic
    fun getHUDByName(targetName: String?): HUDModule {
        for (iModule in hUDModules) {
            if (!iModule.moduleName.equals(targetName, ignoreCase = true)) continue
            return iModule
        }
        //TODO 人走码还在
        //XG42.logger.fatal("HUD " + targetName + " is not exist.Please check twice!");
        return NullHUD
    }

    fun onKeyPressed(key: Int, action: Int): Boolean {
        if (key == 0) {
            return false
        }

        moduleList.forEach {
            if (it.bind == key) {
                if (action == 1) {
                    if (it.holdToEnable) {
                        it.enable()
                    } else {
                        it.toggle()
                    }
                } else if (it.holdToEnable && action == 0) {
                    it.disable()
                }

                if (it is ClickGui || it is HUDEditor) {
                    return true
                }
            }
        }

        return false
    }

    fun onLogout() {
        moduleList.forEach {
            if (it.isEnabled) {
                it.onLogout()
            }
        }
    }
}