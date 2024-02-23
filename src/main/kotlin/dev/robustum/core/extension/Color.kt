package dev.robustum.core.extension

import java.awt.Color

fun averageColor(vararg colors: Color): Color = averageColor(colors.associateWith { 1 })

fun averageColor(colors: Iterable<Color>): Color = averageColor(colors.associateWith { 1 })

fun averageColor(vararg pairs: Pair<Color, Int>): Color = averageColor(mapOf(*pairs))

fun averageColor(map: Map<Color, Int>): Color {
    var redSum = 0
    var greenSum = 0
    var blueSum = 0
    var weightSum = 0
    map.forEach { (color: Color, weight: Int) ->
        // RGB値にweightをかけた値を加算していく
        color.run {
            redSum += this.red * weight
            greenSum += this.green * weight
            blueSum += this.blue * weight
        }
        weightSum += weight
    }
    return if (weightSum > 0) {
        Color(redSum / weightSum, greenSum / weightSum, blueSum / weightSum)
    } else {
        Color.WHITE
    }
}
