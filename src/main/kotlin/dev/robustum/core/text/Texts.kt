package dev.robustum.core.text

import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText

//    Text    //

/**
 * 指定した[文字列][this]を[テキスト][LiteralText]に変換します。
 * @author Hiiragi Tsubasa
 * @since 26.1.0
 */
fun String.toText(): LiteralText = LiteralText(this)

/**
 * 指定した[文字列][value]を翻訳された[テキスト][TranslatableText]に変換します。
 * @author Hiiragi Tsubasa
 * @since 26.1.0
 */
fun translatableText(value: String): TranslatableText = TranslatableText(value)

/**
 * 指定した[文字列][value]と[引数][args]を翻訳された[テキスト][TranslatableText]に変換します。
 * @author Hiiragi Tsubasa
 * @since 26.1.0
 */
fun translatableText(value: String, vararg args: Any): TranslatableText = TranslatableText(value, *args)
