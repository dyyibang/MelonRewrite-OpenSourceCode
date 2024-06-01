package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule

class BooleanSetting(name: String, defaultValue: Boolean) :
        Setting<Boolean>(name, defaultValue), SettingVisibility<BooleanSetting>