package com.example.minhascompras.ui.utils

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
            screenWidth < 360 -> 4.dp
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
            screenWidth < 360 -> 4.dp
            screenWidth < 600 -> 6.dp
            screenWidth < 840 -> 8.dp
            else -> 12.dp
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
     * Retorna tamanho de fonte para nomes de itens baseado no tamanho da tela
     */
    @Composable
    fun getItemNameFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 13.sp  // Telas muito pequenas
            screenWidth < 600 -> 14.sp  // Telas pequenas/médias
            screenWidth < 840 -> 15.sp  // Telas médias/grandes
            else -> 16.sp               // Telas grandes/tablets
        }
    }
    
    /**
     * Retorna tamanho de fonte para valores monetários baseado no tamanho da tela
     */
    @Composable
    fun getPriceFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 10.sp  // Telas muito pequenas
            screenWidth < 600 -> 11.sp  // Telas pequenas/médias
            screenWidth < 840 -> 12.sp  // Telas médias/grandes
            else -> 13.sp                // Telas grandes/tablets
        }
    }
    
    /**
     * Retorna tamanho de fonte para valores de estatísticas baseado no tamanho da tela
     */
    @Composable
    fun getStatisticValueFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 14.sp  // Telas muito pequenas
            screenWidth < 600 -> 16.sp  // Telas pequenas/médias
            screenWidth < 840 -> 18.sp  // Telas médias/grandes
            else -> 20.sp                // Telas grandes/tablets
        }
    }
    
    /**
     * Retorna tamanho de fonte para labels de estatísticas baseado no tamanho da tela
     */
    @Composable
    fun getStatisticLabelFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 9.sp   // Telas muito pequenas
            screenWidth < 600 -> 10.sp  // Telas pequenas/médias
            screenWidth < 840 -> 11.sp  // Telas médias/grandes
            else -> 12.sp                // Telas grandes/tablets
        }
    }
    
    /**
     * Retorna tamanho de fonte para quantidade baseado no tamanho da tela
     */
    @Composable
    fun getQuantityFontSize(): TextUnit {
        val screenWidth = getScreenWidth().value
        return when {
            screenWidth < 360 -> 9.sp   // Telas muito pequenas
            screenWidth < 600 -> 10.sp  // Telas pequenas/médias
            screenWidth < 840 -> 11.sp  // Telas médias/grandes
            else -> 12.sp                // Telas grandes/tablets
        }
    }
}

