package com.routehub.pos.models.settings

data class SettingItem(
    val id: String,
    val title: String,
    val summary: String,
    val type: SettingType
)

enum class SettingType {
    LANGUAGE,
    OTHER
}