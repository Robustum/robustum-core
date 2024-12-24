package dev.robustum.core.text

import com.mojang.serialization.Codec
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * [Text]を[Codec]で変換するインターフェースです。
 */
interface TextSerializer<T : Text> {
    val id: Identifier
    val codec: Codec<T>
}
