package com.arrowescape.master.model

enum class Tier(
    val title: String,
    val badgeSymbol: String,
    val minLevel: Int,
    val maxLevel: Int,
    val hexColor: Long
) {
    NOVICE("Novice", "🌱", 1, 5, 0xFF4ADE80),
    ADEPT("Adept", "⚡", 6, 11, 0xFF38BDF8),
    EXPERT("Expert", "🔥", 12, 24, 0xFFF59E0B),
    MASTER("Master", "👑", 25, 49, 0xFFA855F7),
    GRANDMASTER("Grandmaster", "🔮", 50, 74, 0xFFEC4899),
    MYTHIC("Mythic", "🌌", 75, 99, 0xFF6366F1),
    COSMIC("Cosmic", "✨", 100, 149, 0xFF06B6D4),
    INFINITY("Infinity", "♾️", 150, Int.MAX_VALUE, 0xFFEAB308);

    companion object {
        fun fromLevel(level: Int): Tier {
            return entries.find { level in it.minLevel..it.maxLevel } ?: INFINITY
        }
    }
}
