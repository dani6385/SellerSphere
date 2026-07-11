package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Guardian Console Premium Navy-Slate Palette (inspired by Admin screenshot)
val GuardianDarkBackground = Color(0xFF090C15)       // Deep navy-slate backdrop
val GuardianDarkSurface = Color(0xFF131726)          // Sleek dark card container
val GuardianDarkSurfaceVariant = Color(0xFF1C2237)   // Lighter secondary slate container
val GuardianDarkOutline = Color(0xFF2E3758)          // Muted blue-slate border outline
val GuardianDarkPrimary = Color(0xFF5C62F6)          // Vibrant Indigo-violet primary brand accent
val GuardianDarkSecondary = Color(0xFF8A93A6)        // Clean slate grey secondary tint
val GuardianDarkTertiary = Color(0xFFF472B6)         // Soft pink-violet tertiary brand accent
val GuardianDarkOnPrimary = Color(0xFFFFFFFF)        // High-contrast white
val GuardianDarkOnBackground = Color(0xFFF3F4F6)     // Light off-white contrast text
val GuardianDarkOnSurface = Color(0xFFF3F4F6)        // Light off-white contrast text

// Clean Minimalism Color Palette (Backwards compatibility)
val MinimalistDarkBackground = GuardianDarkBackground
val MinimalistDarkSurface = GuardianDarkSurface
val MinimalistDarkSurfaceVariant = GuardianDarkSurfaceVariant
val MinimalistDarkOutline = GuardianDarkOutline
val MinimalistDarkPrimary = GuardianDarkPrimary
val MinimalistDarkSecondary = GuardianDarkSecondary
val MinimalistDarkTertiary = GuardianDarkTertiary
val MinimalistDarkOnPrimary = GuardianDarkOnPrimary
val MinimalistDarkOnBackground = GuardianDarkOnBackground
val MinimalistDarkOnSurface = GuardianDarkOnSurface

val MinimalistLightBackground = Color(0xFFF5F7FF)     // Soft blue-tinted clean white
val MinimalistLightSurface = Color(0xFFFFFFFF)        // Pure white card container
val MinimalistLightSurfaceVariant = Color(0xFFE2E6F5) // Soft grey-blue container
val MinimalistLightOutline = Color(0xFFC4CADB)        // Muted light border line
val MinimalistLightPrimary = Color(0xFF3B41C5)        // Clean deep primary blue
val MinimalistLightSecondary = Color(0xFF59627A)      // Clean secondary slate
val MinimalistLightTertiary = Color(0xFF7E3EC5)       // Clean tertiary violet
val MinimalistLightOnPrimary = Color(0xFFFFFFFF)      // White contrast
val MinimalistLightOnBackground = Color(0xFF131726)   // Dark contrast for light background
val MinimalistLightOnSurface = Color(0xFF131726)      // Dark contrast for light surface

// Live Sync Indicator & Statuses (derived from screenshot design)
val LiveSyncGreen = Color(0xFF10B981)
val AlertRed = Color(0xFFEF4444)
val PendingOrange = Color(0xFFF59E0B)

// Helper aliases to maintain complete backward compatibility and clean mapping
val MatrixCyan = GuardianDarkPrimary
val MatrixBlue = GuardianDarkSecondary
val MatrixOrange = PendingOrange
val MatrixDarkBackground = GuardianDarkBackground
val MatrixDarkSurface = GuardianDarkSurface
val MatrixDarkOutline = GuardianDarkOutline
val MatrixLightBackground = MinimalistLightBackground
val MatrixLightSurface = MinimalistLightSurface
val MatrixLightOutline = MinimalistLightOutline
val MatrixCyanLight = MinimalistLightPrimary
val MatrixBlueLight = MinimalistLightSecondary

// Direct backward compatibility mapping for the UI elements
val NeonCyan = GuardianDarkPrimary
val ElectricBlue = Color(0xFF3B82F6)
val VividOrchid = GuardianDarkTertiary
val SoftTeal = LiveSyncGreen
val WarmOrange = PendingOrange
val RadiantRose = AlertRed

val SlateDarkBackground = GuardianDarkBackground
val SlateDarkSurface = GuardianDarkSurface
val SlateDarkCard = GuardianDarkSurfaceVariant

val SlateTextPrimary = GuardianDarkOnBackground
val SlateTextSecondary = GuardianDarkSecondary
val SlateBorder = GuardianDarkOutline
