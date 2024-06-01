package dev.zenhao.melon.module

import dev.zenhao.melon.setting.*
import melon.system.util.color.ColorRGB
import java.awt.Color

interface SettingRegistrable {
    fun bsetting(name: String, defaultValue: Boolean): BooleanSetting {
        val value = BooleanSetting(name, defaultValue)
        onRegisterSetting(value)
        return value
    }

    fun isetting(name: String, defaultValue: Int, minValue: Int, maxValue: Int, modifyValue: Int = 0): IntegerSetting {
        val value = IntegerSetting(name, defaultValue, minValue, maxValue, modifyValue)
        onRegisterSetting(value)
        return value
    }

    fun fsetting(
        name: String, defaultValue: Float, minValue: Float, maxValue: Float, modifyValue: Float = 0f
    ): FloatSetting {
        val value = FloatSetting(name, defaultValue, minValue, maxValue, modifyValue)
        onRegisterSetting(value)
        return value
    }

    fun dsetting(
        name: String, defaultValue: Double, minValue: Double, maxValue: Double, modifyValue: Double = 0.0
    ): DoubleSetting {
        val value = DoubleSetting(name, defaultValue, minValue, maxValue, modifyValue)
        onRegisterSetting(value)
        return value
    }

    fun isetting(name: String, defaultValue: Int, minValue: Int, maxValue: Int): IntegerSetting {
        val value = IntegerSetting(name, defaultValue, minValue, maxValue, 0)
        onRegisterSetting(value)
        return value
    }

    fun fsetting(name: String, defaultValue: Float, minValue: Float, maxValue: Float): FloatSetting {
        val value = FloatSetting(name, defaultValue, minValue, maxValue, 0f)
        onRegisterSetting(value)
        return value
    }

    fun dsetting(name: String, defaultValue: Double, minValue: Double, maxValue: Double): DoubleSetting {
        val value = DoubleSetting(name, defaultValue, minValue, maxValue, 0.0)
        onRegisterSetting(value)
        return value
    }

    fun msetting(name: String, modes: Enum<*>): ModeSetting<*> {
        val value: ModeSetting<*> = ModeSetting(name, modes)
        onRegisterSetting(value)
        return value
    }

    fun ssetting(name: String, defaultValue: String): StringSetting {
        val value = StringSetting(name, defaultValue)
        onRegisterSetting(value)
        return value
    }

    fun csetting(name: String, defaultValue: Color): ColorSetting {
        val value = ColorSetting(name, defaultValue)
        onRegisterSetting(value)
        return value
    }

    fun csetting(name: String, defaultValue: ColorRGB): ColorSetting {
        val value = ColorSetting(name, Color(defaultValue.r, defaultValue.g, defaultValue.b))
        onRegisterSetting(value)
        return value
    }

    fun onRegisterSetting(setting: Setting<*>)
}