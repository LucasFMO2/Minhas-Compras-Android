package com.example.minhascompras.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Utilitários para criar layouts responsivos baseados no tamanho da tela
 * Sistema baseado em múltiplos de 4dp para consistência
 */
object ResponsiveUtils {
    
    // Breakpoints precisos para diferentes tamanhos de tela
    private const val BREAKPOINT_SMALL = 360    // Telefones pequenos
    private const val BREAKPOINT_MEDIUM = 600    // Telefones grandes / Tablets pequenos
    private const val BREAKPOINT_LARGE = 840     // Tablets médios
    private const val BREAKPOINT_XLARGE = 1200   // Tablets grandes / Desktops
    
    /**
     * Retorna o tamanho da tela em dp
     */
    @Composable
    fun getScreenWidth(): Dp {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp.dp
    }
    
    /**
     * Retorna o tamanho da tela em dp
     */
    @Composable
    fun getScreenHeight(): Dp {
        val configuration = LocalConfiguration.current
        return configuration.screenHeightDp.dp
    }
    
    /**
     * Retorna padding horizontal baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 12.dp   // 3 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 16.dp  // 4 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 20.dp  // 5 * 4dp
            screenWidth < BREAKPOINT_XLARGE -> 24.dp  // 6 * 4dp
            else -> 32.dp                             // 8 * 4dp (tablets grandes)
        }
    }
    
    /**
     * Retorna padding vertical baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getVerticalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 8.dp    // 2 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 12.dp // 3 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 16.dp  // 4 * 4dp
            else -> 20.dp                            // 5 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento entre elementos baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 8.dp    // 2 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 12.dp  // 3 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 16.dp   // 4 * 4dp
            else -> 20.dp                             // 5 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento pequeno baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getSmallSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 4.dp    // 1 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 6.dp   // 1.5 * 4dp (permitido para espaçamento fino)
            screenWidth < BREAKPOINT_LARGE -> 8.dp    // 2 * 4dp
            else -> 10.dp                             // 2.5 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento médio baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getMediumSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 12.dp   // 3 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 16.dp  // 4 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 20.dp   // 5 * 4dp
            else -> 24.dp                             // 6 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento grande baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getLargeSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 16.dp   // 4 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 24.dp  // 6 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 32.dp   // 8 * 4dp
            else -> 40.dp                             // 10 * 4dp
        }
    }
    
    /**
     * Retorna tamanho de ícone baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getIconSize(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 20.dp   // 5 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 24.dp  // 6 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 28.dp   // 7 * 4dp
            else -> 32.dp                             // 8 * 4dp
        }
    }
    
    /**
     * Retorna padding de card baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getCardPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 12.dp   // 3 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 16.dp  // 4 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 20.dp   // 5 * 4dp
            else -> 24.dp                             // 6 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento entre cards de estatísticas baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getStatisticCardSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 4.dp    // 1 * 4dp
            screenWidth < 480 -> 6.dp                 // 1.5 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 8.dp   // 2 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 12.dp   // 3 * 4dp
            else -> 16.dp                             // 4 * 4dp
        }
    }
    
    /**
     * Retorna padding horizontal de card de estatística baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getStatisticCardHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 8.dp     // 2 * 4dp
            screenWidth < 480 -> 10.dp                 // 2.5 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 12.dp  // 3 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 16.dp   // 4 * 4dp
            else -> 20.dp                             // 5 * 4dp
        }
    }

    /**
     * Retorna padding horizontal aplicado ao container das estatísticas (múltiplos de 4dp)
     */
    @Composable
    fun getStatisticRowHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 0.dp
            screenWidth < 480 -> 4.dp                  // 1 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 8.dp   // 2 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 12.dp   // 3 * 4dp
            else -> 16.dp                             // 4 * 4dp
        }
    }
    
    /**
     * Verifica se a tela é pequena (compacta)
     */
    @Composable
    fun isSmallScreen(): Boolean {
        return getScreenWidth().value < BREAKPOINT_SMALL
    }
    
    /**
     * Verifica se a tela é média
     */
    @Composable
    fun isMediumScreen(): Boolean {
        val width = getScreenWidth().value
        return width >= BREAKPOINT_SMALL && width < BREAKPOINT_MEDIUM
    }
    
    /**
     * Verifica se a tela é grande (tablet)
     */
    @Composable
    fun isLargeScreen(): Boolean {
        return getScreenWidth().value >= BREAKPOINT_MEDIUM
    }
    
    /**
     * Verifica se a tela é extra grande (tablet grande)
     */
    @Composable
    fun isXLargeScreen(): Boolean {
        return getScreenWidth().value >= BREAKPOINT_XLARGE
    }
    
    /**
     * Retorna tamanho de fonte para títulos baseado no tamanho da tela
     */
    @Composable
    fun getTitleFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 18.sp
            screenWidth < BREAKPOINT_MEDIUM -> 20.sp
            screenWidth < BREAKPOINT_LARGE -> 22.sp
            else -> 24.sp
        }
    }
    
    /**
     * Retorna tamanho de fonte para corpo de texto baseado no tamanho da tela
     */
    @Composable
    fun getBodyFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 14.sp
            screenWidth < BREAKPOINT_MEDIUM -> 16.sp
            else -> 16.sp
        }
    }
    
    /**
     * Retorna tamanho de fonte para labels baseado no tamanho da tela
     */
    @Composable
    fun getLabelFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 11.sp
            screenWidth < BREAKPOINT_MEDIUM -> 12.sp
            else -> 12.sp
        }
    }
    
    /**
     * Retorna tamanho mínimo de toque (acessibilidade) baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getMinimumTouchTarget(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 40.dp   // 10 * 4dp (mínimo para telas pequenas)
            else -> 48.dp                             // 12 * 4dp (padrão Material Design)
        }
    }
    
    /**
     * Retorna tamanho de botão baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getButtonHeight(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 40.dp   // 10 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 44.dp  // 11 * 4dp
            else -> 48.dp                             // 12 * 4dp
        }
    }
    
    /**
     * Retorna padding de botão baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getButtonPadding(): PaddingValues {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)   // 3*4dp, 2*4dp
            screenWidth < BREAKPOINT_MEDIUM -> PaddingValues(horizontal = 16.dp, vertical = 10.dp) // 4*4dp, 2.5*4dp
            else -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)                            // 6*4dp, 3*4dp
        }
    }
    
    /**
     * Retorna elevação de card baseado no tamanho da tela
     */
    @Composable
    fun getCardElevation(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 1.dp
            screenWidth < BREAKPOINT_MEDIUM -> 2.dp
            else -> 3.dp
        }
    }
    
    /**
     * Retorna raio de borda de card baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getCardCornerRadius(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 12.dp   // 3 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 16.dp  // 4 * 4dp
            else -> 20.dp                             // 5 * 4dp
        }
    }
    
    /**
     * Retorna espaçamento vertical entre seções baseado no tamanho da tela (múltiplos de 4dp)
     */
    @Composable
    fun getSectionSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < BREAKPOINT_SMALL -> 12.dp   // 3 * 4dp
            screenWidth < BREAKPOINT_MEDIUM -> 16.dp  // 4 * 4dp
            screenWidth < BREAKPOINT_LARGE -> 20.dp   // 5 * 4dp
            else -> 24.dp                             // 6 * 4dp
        }
    }
}

