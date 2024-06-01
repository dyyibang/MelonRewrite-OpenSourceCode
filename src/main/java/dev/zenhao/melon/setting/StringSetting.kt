package dev.zenhao.melon.setting

import dev.zenhao.melon.module.AbstractModule

class StringSetting(name: String, defaultValue: String) : Setting<String>(name, defaultValue), SettingVisibility<StringSetting>
