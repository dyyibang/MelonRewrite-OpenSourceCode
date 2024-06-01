package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule

class IntegerSetting(
    name: String,
    defaultValue: Int,
    override var min: Int,
    override var max: Int,
    var modify: Int = 0
) : NumberSetting<Int>(name, defaultValue), SettingVisibility<IntegerSetting> {
    override fun setValueFromString(value: Float) {
        this.value = value.toInt().coerceIn(min, max)
    }
}