package com.lpmoon.asset.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object ThemeManager {
    private val _currentTheme = mutableStateOf(ThemeScheme.FRESH_PROFESSIONAL)
    val currentThemeState: State<ThemeScheme> = _currentTheme
    val currentTheme: ThemeScheme get() = _currentTheme.value

    fun setTheme(theme: ThemeScheme) {
        _currentTheme.value = theme
        // TODO: 保存主题到SharedPreferences
    }

    fun getLightColorScheme(theme: ThemeScheme): ColorScheme {
        return when (theme) {
            ThemeScheme.FRESH_PROFESSIONAL -> lightColorScheme(
                primary = Color(0xFF1976D2),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1E3FF),
                onPrimaryContainer = Color(0xFF001A41),
                secondary = Color(0xFF4CAF50),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFC8E6C9),
                onSecondaryContainer = Color(0xFF0A2F10),
                tertiary = Color(0xFF2196F3),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFD1E4FF),
                onTertiaryContainer = Color(0xFF001D36),
                surface = Color.White,
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFDFE2EB),
                onSurfaceVariant = Color(0xFF43474E),
                outlineVariant = Color(0xFFC3C6CF),
                error = Color(0xFFBA1A1A),
                onError = Color.White
            )
            ThemeScheme.SERIOUS_FINANCE -> lightColorScheme(
                primary = Color(0xFF1E3A8A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD8E2FF),
                onPrimaryContainer = Color(0xFF00174B),
                secondary = Color(0xFF0F766E),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFA7F0E9),
                onSecondaryContainer = Color(0xFF00201D),
                tertiary = Color(0xFF9333EA),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFF0DBFF),
                onTertiaryContainer = Color(0xFF2E0054),
                surface = Color(0xFFF8FAFC),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFE0E2EC),
                onSurfaceVariant = Color(0xFF44474F),
                outlineVariant = Color(0xFFC4C6D0),
                error = Color(0xFFBA1A1A),
                onError = Color.White
            )
            ThemeScheme.VIBRANT_MODERN -> lightColorScheme(
                primary = Color(0xFF6366F1),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE0E0FF),
                onPrimaryContainer = Color(0xFF020069),
                secondary = Color(0xFFEC4899),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFD9E7),
                onSecondaryContainer = Color(0xFF3C0027),
                tertiary = Color(0xFFF59E0B),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFDDB6),
                onTertiaryContainer = Color(0xFF271900),
                surface = Color.White,
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFE0E2EC),
                onSurfaceVariant = Color(0xFF44474F),
                outlineVariant = Color(0xFFC4C6D0),
                error = Color(0xFFBA1A1A),
                onError = Color.White
            )
            ThemeScheme.NATURAL_WARM -> lightColorScheme(
                primary = Color(0xFFD97706),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFDDB6),
                onPrimaryContainer = Color(0xFF271900),
                secondary = Color(0xFF059669),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFA7F0E9),
                onSecondaryContainer = Color(0xFF00201D),
                tertiary = Color(0xFF7C3AED),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFF0DBFF),
                onTertiaryContainer = Color(0xFF2E0054),
                surface = Color(0xFFFEF3C7),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFF0E1C2),
                onSurfaceVariant = Color(0xFF4D4639),
                outlineVariant = Color(0xFFD3C5A7),
                error = Color(0xFFBA1A1A),
                onError = Color.White
            )
            ThemeScheme.MINIMAL_MONOCHROME -> lightColorScheme(
                primary = Color(0xFF374151),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE5E7EB),
                onPrimaryContainer = Color(0xFF111827),
                secondary = Color(0xFF6B7280),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE5E7EB),
                onSecondaryContainer = Color(0xFF1F2937),
                tertiary = Color(0xFF9CA3AF),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFE5E7EB),
                onTertiaryContainer = Color(0xFF374151),
                surface = Color(0xFFF9FAFB),
                onSurface = Color(0xFF374151),
                surfaceVariant = Color(0xFFE5E7EB),
                onSurfaceVariant = Color(0xFF6B7280),
                outlineVariant = Color(0xFFD1D5DB),
                error = Color(0xFFEF4444),
                onError = Color.White
            )
            ThemeScheme.HIGH_CONTRAST -> lightColorScheme(
                primary = Color.Black,
                onPrimary = Color.White,
                primaryContainer = Color(0xFF333333),
                onPrimaryContainer = Color.White,
                secondary = Color(0xFF0057B7),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD4E5FF),
                onSecondaryContainer = Color(0xFF001D3D),
                tertiary = Color(0xFFD52B1E),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFDAD6),
                onTertiaryContainer = Color(0xFF410002),
                surface = Color.White,
                onSurface = Color.Black,
                surfaceVariant = Color(0xFFEEEEEE),
                onSurfaceVariant = Color(0xFF444444),
                outlineVariant = Color(0xFF888888),
                error = Color(0xFFBA1A1A),
                onError = Color.White
            )
        }
    }

    fun getDarkColorScheme(theme: ThemeScheme): ColorScheme {
        return when (theme) {
            ThemeScheme.FRESH_PROFESSIONAL -> darkColorScheme(
                primary = Color(0xFF9EC7FF),
                onPrimary = Color(0xFF002F67),
                primaryContainer = Color(0xFF004590),
                onPrimaryContainer = Color(0xFFD1E3FF),
                secondary = Color(0xFFA8D5A9),
                onSecondary = Color(0xFF1D361E),
                secondaryContainer = Color(0xFF344D35),
                onSecondaryContainer = Color(0xFFC8E6C9),
                tertiary = Color(0xFFA4C9FF),
                onTertiary = Color(0xFF003259),
                tertiaryContainer = Color(0xFF00497E),
                onTertiaryContainer = Color(0xFFD1E4FF),
                surface = Color(0xFF1A1C1E),
                onSurface = Color(0xFFE2E2E6),
                surfaceVariant = Color(0xFF43474E),
                onSurfaceVariant = Color(0xFFC3C6CF),
                outlineVariant = Color(0xFF43474E),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            ThemeScheme.SERIOUS_FINANCE -> darkColorScheme(
                primary = Color(0xFFADC7FF),
                onPrimary = Color(0xFF002A77),
                primaryContainer = Color(0xFF003BA6),
                onPrimaryContainer = Color(0xFFD8E2FF),
                secondary = Color(0xFF8CD8CF),
                onSecondary = Color(0xFF003734),
                secondaryContainer = Color(0xFF00504B),
                onSecondaryContainer = Color(0xFFA7F0E9),
                tertiary = Color(0xFFDEB9FF),
                onTertiary = Color(0xFF4C007E),
                tertiaryContainer = Color(0xFF6A00AF),
                onTertiaryContainer = Color(0xFFF0DBFF),
                surface = Color(0xFF0F172A),
                onSurface = Color(0xFFE2E2E6),
                surfaceVariant = Color(0xFF44474F),
                onSurfaceVariant = Color(0xFFC4C6D0),
                outlineVariant = Color(0xFF44474F),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            ThemeScheme.VIBRANT_MODERN -> darkColorScheme(
                primary = Color(0xFFC0C2FF),
                onPrimary = Color(0xFF1F006F),
                primaryContainer = Color(0xFF383A8B),
                onPrimaryContainer = Color(0xFFE0E0FF),
                secondary = Color(0xFFFFB0D9),
                onSecondary = Color(0xFF5C1143),
                secondaryContainer = Color(0xFF7D295F),
                onSecondaryContainer = Color(0xFFFFD9E7),
                tertiary = Color(0xFFFFB95C),
                onTertiary = Color(0xFF422C00),
                tertiaryContainer = Color(0xFF5E4100),
                onTertiaryContainer = Color(0xFFFFDDB6),
                surface = Color(0xFF1F2937),
                onSurface = Color(0xFFE2E2E6),
                surfaceVariant = Color(0xFF44474F),
                onSurfaceVariant = Color(0xFFC4C6D0),
                outlineVariant = Color(0xFF44474F),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            ThemeScheme.NATURAL_WARM -> darkColorScheme(
                primary = Color(0xFFFFB95C),
                onPrimary = Color(0xFF422C00),
                primaryContainer = Color(0xFF5E4100),
                onPrimaryContainer = Color(0xFFFFDDB6),
                secondary = Color(0xFF8CD8CF),
                onSecondary = Color(0xFF003734),
                secondaryContainer = Color(0xFF00504B),
                onSecondaryContainer = Color(0xFFA7F0E9),
                tertiary = Color(0xFFDEB9FF),
                onTertiary = Color(0xFF4C007E),
                tertiaryContainer = Color(0xFF6A00AF),
                onTertiaryContainer = Color(0xFFF0DBFF),
                surface = Color(0xFF1C1917),
                onSurface = Color(0xFFEDE0DA),
                surfaceVariant = Color(0xFF4D4639),
                onSurfaceVariant = Color(0xFFD3C5A7),
                outlineVariant = Color(0xFF4D4639),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
            ThemeScheme.MINIMAL_MONOCHROME -> darkColorScheme(
                primary = Color(0xFFD1D5DB),
                onPrimary = Color(0xFF1F2937),
                primaryContainer = Color(0xFF374151),
                onPrimaryContainer = Color(0xFFE5E7EB),
                secondary = Color(0xFF9CA3AF),
                onSecondary = Color(0xFF374151),
                secondaryContainer = Color(0xFF4B5563),
                onSecondaryContainer = Color(0xFFE5E7EB),
                tertiary = Color(0xFF6B7280),
                onTertiary = Color(0xFF1F2937),
                tertiaryContainer = Color(0xFF374151),
                onTertiaryContainer = Color(0xFFE5E7EB),
                surface = Color(0xFF111827),
                onSurface = Color(0xFFD1D5DB),
                surfaceVariant = Color(0xFF374151),
                onSurfaceVariant = Color(0xFF9CA3AF),
                outlineVariant = Color(0xFF4B5563),
                error = Color(0xFFFCA5A5),
                onError = Color(0xFF7F1D1D)
            )
            ThemeScheme.HIGH_CONTRAST -> darkColorScheme(
                primary = Color.White,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFFCCCCCC),
                onPrimaryContainer = Color.Black,
                secondary = Color(0xFF4DABF7),
                onSecondary = Color.Black,
                secondaryContainer = Color(0xFF003A75),
                onSecondaryContainer = Color(0xFFD4E5FF),
                tertiary = Color(0xFFFF6B6B),
                onTertiary = Color.Black,
                tertiaryContainer = Color(0xFF93000A),
                onTertiaryContainer = Color(0xFFFFDAD6),
                surface = Color.Black,
                onSurface = Color.White,
                surfaceVariant = Color(0xFF222222),
                onSurfaceVariant = Color(0xFFCCCCCC),
                outlineVariant = Color(0xFF666666),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )
        }
    }
}