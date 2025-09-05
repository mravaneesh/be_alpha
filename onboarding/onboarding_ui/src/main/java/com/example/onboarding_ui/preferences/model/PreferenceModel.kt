package com.example.onboarding_ui.preferences.model

data class PreferenceItem(
    val id: String,
    val title: String,
    val emoji: String? = null,
    var isSelected: Boolean = false
)

data class PreferenceSection(
    val sectionTitle: String,
    val items: List<PreferenceItem>,
    val isMultiSelect: Boolean = true
)

