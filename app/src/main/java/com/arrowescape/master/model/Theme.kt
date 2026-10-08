package com.arrowescape.master.model

import androidx.compose.ui.graphics.Color

enum class ThemeType(val displayName: String) {
    NEON_CYBER("Neon Cyber"),
    SUNSET_BLAZE("Sunset Blaze"),
    EMERALD_MATRIX("Emerald Matrix"),
    DARK_GOLD("Dark Gold");

    fun next(): ThemeType {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }
}

data class ThemeColors(
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val cardBackground: Color,
    val boardBackground: Color,
    val gridLine: Color,
    val arrowPrimary: Color,
    val arrowGlow: Color,
    val arrowSecondary: Color,
    val rotatorPivot: Color,
    val shieldArmor: Color,
    val rockFill: Color,
    val rockBorder: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

object ThemePalettes {
    val NeonCyber = ThemeColors(
        backgroundStart = Color(0xFF060B19),
        backgroundEnd = Color(0xFF0F172A),
        cardBackground = Color(0x1F1E293B),
        boardBackground = Color(0x33020617),
        gridLine = Color(0x1A38BDF8),
        arrowPrimary = Color(0xFF06B6D4),
        arrowGlow = Color(0xFF22D3EE),
        arrowSecondary = Color(0xFFEC4899),
        rotatorPivot = Color(0xFFF43F5E),
        shieldArmor = Color(0xFF3B82F6),
        rockFill = Color(0xFF334155),
        rockBorder = Color(0xFF64748B),
        accent = Color(0xFFA855F7),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8)
    )

    val SunsetBlaze = ThemeColors(
        backgroundStart = Color(0xFF1A0A10),
        backgroundEnd = Color(0xFF2D121E),
        cardBackground = Color(0x2E3F1E28),
        boardBackground = Color(0x3D18070E),
        gridLine = Color(0x26F97316),
        arrowPrimary = Color(0xFFF97316),
        arrowGlow = Color(0xFFFDBA74),
        arrowSecondary = Color(0xFFE11D48),
        rotatorPivot = Color(0xFFFACC15),
        shieldArmor = Color(0xFFC084FC),
        rockFill = Color(0xFF451A22),
        rockBorder = Color(0xFF9F1239),
        accent = Color(0xFFFB923C),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFFDA4AF)
    )

    val EmeraldMatrix = ThemeColors(
        backgroundStart = Color(0xFF021008),
        backgroundEnd = Color(0xFF092013),
        cardBackground = Color(0x1A0F381E),
        boardBackground = Color(0x3302190A),
        gridLine = Color(0x2610B981),
        arrowPrimary = Color(0xFF10B981),
        arrowGlow = Color(0xFF6EE7B7),
        arrowSecondary = Color(0xFF059669),
        rotatorPivot = Color(0xFF84CC16),
        shieldArmor = Color(0xFF06B6D4),
        rockFill = Color(0xFF1C3D2B),
        rockBorder = Color(0xFF054F31),
        accent = Color(0xFF34D399),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFFA7F3D0)
    )

    val DarkGold = ThemeColors(
        backgroundStart = Color(0xFF14100B),
        backgroundEnd = Color(0xFF221A10),
        cardBackground = Color(0x293D2C1A),
        boardBackground = Color(0x3D1A1308),
        gridLine = Color(0x26EAB308),
        arrowPrimary = Color(0xFFEAB308),
        arrowGlow = Color(0xFFFDE047),
        arrowSecondary = Color(0xFFCA8A04),
        rotatorPivot = Color(0xFFF97316),
        shieldArmor = Color(0xFF60A5FA),
        rockFill = Color(0xFF382E21),
        rockBorder = Color(0xFF854D0E),
        accent = Color(0xFFFACC15),
        textPrimary = Color(0xFFFEFCE8),
        textSecondary = Color(0xFFFDE68A)
    )

    fun get(themeType: ThemeType): ThemeColors {
        return when (themeType) {
            ThemeType.NEON_CYBER -> NeonCyber
            ThemeType.SUNSET_BLAZE -> SunsetBlaze
            ThemeType.EMERALD_MATRIX -> EmeraldMatrix
            ThemeType.DARK_GOLD -> DarkGold
        }
    }
}
