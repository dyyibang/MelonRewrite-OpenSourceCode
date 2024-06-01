package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule

class FloatSetting(
    name: String,
    defaultValue: Float,
    override var min: Float,
    override var max: Float,
    var modify: Float = 0f
): NumberSetting<Float>(name, defaultValue), SettingVisibility<FloatSetting> {
    override fun setValueFromString(value: Float) {
        this.value = value.coerceIn(min, max)
    }
}