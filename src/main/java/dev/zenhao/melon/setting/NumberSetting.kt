package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule
import dev.zenhao.melon.utils.animations.MathUtils

abstract class NumberSetting<T : Number>(
    name: String,
    defaultValue: T,
) : Setting<T>(name, defaultValue) {
    abstract var max: T
    abstract var min: T

    @Suppress("UNCHECKED_CAST")
    var percent: Float
        get() = ((value.toFloat() - min.toFloat()) / (max.toFloat() - min.toFloat())).coerceIn(0f, 1f)
        set(percent) {
            val value = ((max.toFloat() - min.toFloat()) * percent.coerceIn(0f, 1f)) + min.toFloat()
            setValueFromString(MathUtils.round(value, 2))
        }

    abstract fun setValueFromString(value: Float)
}