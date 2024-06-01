package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule

class DoubleSetting(
    name: String,
    defaultValue: Double,
    override var min: Double,
    override var max: Double,
    var modify: Double = 0.0
) : NumberSetting<Double>(name, defaultValue), SettingVisibility<DoubleSetting> {
    override fun setValueFromString(value: Float) {
        this.value = value.toDouble().coerceIn(min, max)
    }
}