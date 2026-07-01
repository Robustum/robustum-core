package dev.robustum.core.text

import net.minecraft.text.Text

/**
 * [テキスト][Text]を提供するインターフェースです。
 *
 * 参照 : [Mekanism - IHasTextComponent](https://github.com/mekanism/Mekanism/blob/26.1/src/api/java/mekanism/api/text/IHasTextComponent.java)
 */
fun interface HasText {
    /**
     * [テキスト][Text]を取得します。
     */
    fun getText(): Text
}
