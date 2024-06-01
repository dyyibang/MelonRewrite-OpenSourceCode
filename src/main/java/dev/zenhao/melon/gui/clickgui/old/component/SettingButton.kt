package dev.zenhao.melon.gui.clickgui.old.component

import dev.zenhao.melon.setting.ModeSetting
import dev.zenhao.melon.setting.Setting

abstract class SettingButton<T: Any> : Component() {
    lateinit var value: Setting<T>
    val asModeValue: ModeSetting<*> get() = value as ModeSetting<*>
}
