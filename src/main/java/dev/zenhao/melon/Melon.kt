package dev.zenhao.melon

import dev.zenhao.melon.command.CommandManager
import dev.zenhao.melon.manager.*
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.client.ClickGui
import dev.zenhao.melon.module.modules.client.HUDEditor
import dev.zenhao.melon.setting.StringSetting
import dev.zenhao.melon.utils.math.LagCompensator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import melon.system.event.AlwaysListening
import melon.utils.threads.BackgroundScope
import org.apache.logging.log4j.LogManager
import verify.EncryptionConnectionFactory
import verify.SocketConnection
import verify.SocketDebugger
import verify.SocketDebugger.initState
import kotlin.io.path.Path

//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖保佑             永无BUG
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？

// 程序出Bug了？
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正不是我写的
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣
// 万一是我写的呢
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正改了一个又出三个
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣

/**
 *                      江城子 . 程序员之歌
 *
 *                  十年生死两茫茫，写程序，到天亮。
 *                      千行代码，Bug何处藏。
 *                  纵使上线又怎样，朝令改，夕断肠。
 *
 *                  领导每天新想法，天天改，日日忙。
 *                      相顾无言，惟有泪千行。
 *                  每晚灯火阑珊处，夜难寐，加班狂。
 */

class Melon : AlwaysListening {
    enum class UserType(val userType: String) {
        User("User"), Beta("Beta"), Nigger("NIGGER"), None("None")
    }

    companion object {
        const val MOD_NAME = "Melon"
        const val VERSION = "1.7"
        var userState = UserType.Beta
        var logger = LogManager.getLogger("Melon")
        var commandPrefix = StringSetting("CommandPrefix", ".")
        var DISPLAY_NAME = "$MOD_NAME $VERSION (${userState.userType})"
        var TICK_TIMER = 1f

        //如果是Dev版本就改成"正数" User版本就改成负数
        var verifiedState = 1

        // Root Dir Save
        val DIRECTORY = Path("$MOD_NAME/")

        @get:JvmName("isReady")
        var ready = false; private set
        var hasInit = false
        var hasPostInit = false
        var called = false
            set(value) {
                //SocketConnection.call(EncryptionConnectionFactory)
                field = value
            }

        fun onManagersInit() = runBlocking {
            if (hasInit) return@runBlocking
//            defaultScope.launch {
//                dumpJar()
//            }
            Thread.currentThread().priority = Thread.MAX_PRIORITY
            FileManager.backupConfigFolder()
            //while (true) if (initState == 0) delay(10) else break
            //delay(10)

            if (verifiedState <= 0) {
                hasInit = true
                return@runBlocking
            }

            LagCompensator.call()
            EventListenerManager.call()
            ModuleManager.init()
            CommandManager.onInit()
            RotationManager.onInit()
            FileManager.onInit()
            FileManager.loadAll()
            InventoryTaskManager.onInit()
            CrystalManager.onInit()
            HotbarManager.onInit()
            EntityManager.onInit()
            MovementManager.onInit()
            CombatManager.onInit()
            HoleManager.onInit()
            WorldManager.onInit()
            BlockFinderManager.onInit()
            SphereCalculatorManager.onInit()
            DisablerManager.onInit()
            BackgroundScope.start()

            ClickGui.disable()
            HUDEditor.disable()
            hasInit = true
        }

        fun onPostInit() {
            if (hasPostInit) return
            //SocketDebugger.tryReset(initState != 1)
            for (module in ModuleManager.getToggleList()) {
                if (module.isDisabled) continue
                module.disable()
            }
            ready = true
            hasPostInit = true
        }
    }
}