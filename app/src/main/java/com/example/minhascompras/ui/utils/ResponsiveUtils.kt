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
 */
object ResponsiveUtils {
    
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
     * Retorna padding horizontal baseado no tamanho da tela
     */
    @Composable
    fun getHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 12.dp  // Telas muito pequenas
            screenWidth < 600 -> 16.dp  // Telas pequenas/médias
            screenWidth < 840 -> 20.dp  // Telas médias/grandes
            else -> 24.dp              // Telas grandes/tablets
        }
    }
    
    /**
     * Retorna padding vertical baseado no tamanho da tela
     */
    @Composable
    fun getVerticalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 8.dp
            screenWidth < 600 -> 12.dp
            screenWidth < 840 -> 16.dp
            else -> 20.dp
        }
    }
    
    /**
     * Retorna espaçamento entre elementos baseado no tamanho da tela
     */
    @Composable
    fun getSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 8.dp
            screenWidth < 600 -> 12.dp
            screenWidth < 840 -> 16.dp
            else -> 20.dp
        }
    }
    
    /**
     * Retorna espaçamento pequeno baseado no tamanho da tela
     */
    @Composable
    fun getSmallSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 4.dp
            screenWidth < 600 -> 6.dp
            screenWidth < 840 -> 8.dp
            else -> 10.dp
        }
    }
    
    /**
     * Retorna tamanho de ícone baseado no tamanho da tela
     */
    @Composable
    fun getIconSize(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 20.dp
            screenWidth < 600 -> 24.dp
            screenWidth < 840 -> 28.dp
            else -> 32.dp
        }
    }
    
    /**
     * Retorna padding de card baseado no tamanho da tela
     */
    @Composable
    fun getCardPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 12.dp
            screenWidth < 600 -> 16.dp
            screenWidth < 840 -> 20.dp
            else -> 24.dp
        }
    }
    
    /**
     * Retorna espaçamento entre cards de estatísticas baseado no tamanho da tela
     */
    @Composable
    fun getStatisticCardSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 2.dp
            screenWidth < 480 -> 4.dp
            screenWidth < 600 -> 6.dp
            screenWidth < 840 -> 8.dp
            else -> 12.dp
        }
    }
    
    /**
     * Retorna padding horizontal de card de estatística baseado no tamanho da tela
     */
    @Composable
    fun getStatisticCardHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 2.dp
            screenWidth < 480 -> 4.dp
            screenWidth < 600 -> 6.dp
            screenWidth < 840 -> 8.dp
            else -> 12.dp
        }
    }

    /**
     * Retorna padding horizontal aplicado ao container das estatísticas
     */
    @Composable
    fun getStatisticRowHorizontalPadding(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 0.dp
            screenWidth < 480 -> 4.dp
            screenWidth < 600 -> 8.dp
            screenWidth < 840 -> 12.dp
            else -> 16.dp
        }
    }
    
    /**
     * Verifica se a tela é pequena (compacta)
     */
    @Composable
    fun isSmallScreen(): Boolean {
        return getScreenWidth().value < 360
    }
    
    /**
     * Verifica se a tela é média
     */
    @Composable
    fun isMediumScreen(): Boolean {
        val width = getScreenWidth().value
        return width >= 360 && width < 600
    }
    
    /**
     * Verifica se a tela é grande (tablet)
     */
    @Composable
    fun isLargeScreen(): Boolean {
        return getScreenWidth().value >= 600
    }
    
    /**
     * Retorna tamanho de fonte para títulos baseado no tamanho da tela
     */
    @Composable
    fun getTitleFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 18.sp
            screenWidth < 600 -> 20.sp
            screenWidth < 840 -> 22.sp
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
            screenWidth < 360 -> 14.sp
            screenWidth < 600 -> 16.sp
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
            screenWidth < 360 -> 11.sp
            screenWidth < 600 -> 12.sp
            else -> 12.sp
        }
    }
    
    /**
     * Retorna tamanho mínimo de toque (acessibilidade) baseado no tamanho da tela
     */
    @Composable
    fun getMinimumTouchTarget(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 40.dp  // Mínimo para telas pequenas
            else -> 48.dp  // Padrão Material Design
        }
    }
    
    /**
     * Retorna tamanho de botão baseado no tamanho da tela
     */
    @Composable
    fun getButtonHeight(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 36.dp
            screenWidth < 600 -> 40.dp
            else -> 48.dp
        }
    }
    
    /**
     * Retorna padding de botão baseado no tamanho da tela
     */
    @Composable
    fun getButtonPadding(): PaddingValues {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            screenWidth < 600 -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            else -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        }
    }
    
    /**
     * Retorna tamanho de card baseado no tamanho da tela
     */
    @Composable
    fun getCardElevation(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 1.dp
            screenWidth < 600 -> 2.dp
            else -> 3.dp
        }
    }
    
    /**
     * Retorna raio de borda de card baseado no tamanho da tela
     */
    @Composable
    fun getCardCornerRadius(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 12.dp
            screenWidth < 600 -> 16.dp
            else -> 16.dp
        }
    }
    
    /**
     * Retorna espaçamento vertical entre seções baseado no tamanho da tela
     */
    @Composable
    fun getSectionSpacing(): Dp {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 12.dp
            screenWidth < 600 -> 16.dp
            screenWidth < 840 -> 20.dp
            else -> 24.dp
        }
    }
}

