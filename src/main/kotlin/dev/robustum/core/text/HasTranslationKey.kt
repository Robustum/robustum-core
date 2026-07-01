package dev.robustum.core.text

/**
 * 翻訳キーを提供するインターフェースです。
 *
 * 参照 : [Mekanism - IHasTranslationKey](https://github.com/mekanism/Mekanism/blob/26.1/src/api/java/mekanism/api/text/IHasTranslationKey.java)
 */
interface HasTranslationKey {
    /**
     * 翻訳キーの値
     */
    val translationKey: String
}
